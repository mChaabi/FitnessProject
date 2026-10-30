package com.example.fitnessBackEnd.service;

import com.example.fitnessBackEnd.dto.CheckInRequest;
import com.example.fitnessBackEnd.dto.CheckInResult;
import com.example.fitnessBackEnd.dto.CheckInRow;
import com.example.fitnessBackEnd.dto.TodayStats;
import com.example.fitnessBackEnd.entity.CheckIn;
import com.example.fitnessBackEnd.entity.Member;
import com.example.fitnessBackEnd.entity.Subscription;
import com.example.fitnessBackEnd.enums.CheckInOutcome;
import com.example.fitnessBackEnd.enums.DenyReason;
import com.example.fitnessBackEnd.enums.MemberStatus;
import com.example.fitnessBackEnd.enums.SubscriptionStatus;
import com.example.fitnessBackEnd.exception.BadRequestException;
import com.example.fitnessBackEnd.repository.CheckInRepository;
import com.example.fitnessBackEnd.repository.MemberRepository;
import com.example.fitnessBackEnd.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CheckInService {

    private static final int DUPLICATE_MINUTES = 5;
    private static final int EXPIRING_DAYS = 7;

    private final MemberRepository memberRepo;
    private final SubscriptionRepository subRepo;
    private final CheckInRepository repo;

    @Transactional
    public CheckInResult check(CheckInRequest r, String staff) {
        boolean hasQr = r.qrCode() != null && !r.qrCode().isBlank();
        if (hasQr == (r.memberId() != null)) {
            throw new BadRequestException("Indiquez un QR ou un membre, pas les deux");
        }

        Optional<Member> found = hasQr ? memberRepo.findByQrCode(r.qrCode().trim())
                : memberRepo.findById(r.memberId());
        LocalDateTime now = LocalDateTime.now();
        if (found.isEmpty()) {
            return new CheckInResult(CheckInOutcome.DENIED, DenyReason.NOT_FOUND, msg(DenyReason.NOT_FOUND),
                    null, null, null, null, null, false, now);
        }

        Member m = found.get();
        LocalDate today = now.toLocalDate();
        Optional<Subscription> sub = subRepo.findCovering(m.getId(), SubscriptionStatus.ACTIVE, today)
                .stream().findFirst();

        DenyReason reason = null;
        if (m.getStatus() == MemberStatus.INACTIVE) reason = DenyReason.INACTIVE;
        else if (m.getStatus() == MemberStatus.FROZEN) reason = DenyReason.FROZEN;
        else if (sub.isEmpty()) reason = subRepo.existsByMemberId(m.getId()) ? DenyReason.EXPIRED : DenyReason.NO_SUBSCRIPTION;

        if (reason != null) {
            repo.save(CheckIn.builder().member(m).checkedAt(now).outcome(CheckInOutcome.DENIED)
                    .reason(reason).performedBy(staff).build());
            return result(CheckInOutcome.DENIED, reason, m, sub.orElse(null), today, now);
        }

        if (repo.existsByMemberIdAndOutcomeAndCheckedAtAfter(m.getId(), CheckInOutcome.ALLOWED,
                now.minusMinutes(DUPLICATE_MINUTES))) {
            return result(CheckInOutcome.DUPLICATE, null, m, sub.get(), today, now);   // no se guarda
        }

        repo.save(CheckIn.builder().member(m).checkedAt(now).outcome(CheckInOutcome.ALLOWED)
                .performedBy(staff).build());
        return result(CheckInOutcome.ALLOWED, null, m, sub.get(), today, now);
    }

    @Transactional(readOnly = true)
    public List<CheckInRow> today() {
        LocalDate d = LocalDate.now();
        return repo.findBetween(d.atStartOfDay(), d.plusDays(1).atStartOfDay(), PageRequest.of(0, 50)).stream()
                .map(c -> new CheckInRow(c.getId(), c.getMember().getId(),
                        c.getMember().getFirstName() + " " + c.getMember().getLastName(),
                        c.getCheckedAt(), c.getOutcome(), c.getReason()))
                .toList();
    }

    @Transactional(readOnly = true)
    public TodayStats stats() {
        LocalDate d = LocalDate.now();
        LocalDateTime from = d.atStartOfDay(), to = d.plusDays(1).atStartOfDay();
        return new TodayStats(repo.countMembers(CheckInOutcome.ALLOWED, from, to),
                repo.countMembers(CheckInOutcome.DENIED, from, to));
    }

    private CheckInResult result(CheckInOutcome o, DenyReason reason, Member m, Subscription s,
                                 LocalDate today, LocalDateTime now) {
        Long daysLeft = s == null ? null : ChronoUnit.DAYS.between(today, s.getEndDate());
        String message = o == CheckInOutcome.ALLOWED ? "Bienvenue !"
                : o == CheckInOutcome.DUPLICATE ? "Déjà enregistré à l'instant" : msg(reason);
        return new CheckInResult(o, reason, message, m.getId(), m.getFirstName() + " " + m.getLastName(),
                s == null ? null : s.getPlan().getName(), s == null ? null : s.getEndDate(), daysLeft,
                o != CheckInOutcome.DENIED && daysLeft != null && daysLeft <= EXPIRING_DAYS, now);
    }

    private String msg(DenyReason r) {
        return switch (r) {
            case NOT_FOUND -> "QR code inconnu";
            case INACTIVE -> "Compte membre désactivé";
            case FROZEN -> "Abonnement gelé";
            case NO_SUBSCRIPTION -> "Aucun abonnement";
            case EXPIRED -> "Abonnement expiré";
        };
    }
}
