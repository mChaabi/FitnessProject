package com.example.fitnessBackEnd.service;

import com.example.fitnessBackEnd.dto.FreezeRequest;
import com.example.fitnessBackEnd.dto.FreezeResult;
import com.example.fitnessBackEnd.dto.FreezeRow;
import com.example.fitnessBackEnd.entity.Booking;
import com.example.fitnessBackEnd.entity.Member;
import com.example.fitnessBackEnd.entity.MembershipFreeze;
import com.example.fitnessBackEnd.entity.Subscription;
import com.example.fitnessBackEnd.enums.BookingStatus;
import com.example.fitnessBackEnd.enums.EmailType;
import com.example.fitnessBackEnd.enums.MemberStatus;
import com.example.fitnessBackEnd.enums.SubscriptionStatus;
import com.example.fitnessBackEnd.exception.BadRequestException;
import com.example.fitnessBackEnd.exception.ConflictException;
import com.example.fitnessBackEnd.exception.NotFoundException;
import com.example.fitnessBackEnd.repository.BookingRepository;
import com.example.fitnessBackEnd.repository.FreezeRepository;
import com.example.fitnessBackEnd.repository.MemberRepository;
import com.example.fitnessBackEnd.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FreezeService {

    private static final String ACTIVE = "ACTIVE", ENDED = "ENDED";

    private final MemberRepository members;
    private final SubscriptionRepository subs;
    private final FreezeRepository freezes;
    private final BookingRepository bookings;
    private final BookingService bookingService;
    private final EmailQueue emailQueue;
    private final AuditService audit;

    @Value("${app.freeze.min-days:7}") private int minDays;
    @Value("${app.freeze.max-days:90}") private int maxDays;

    // ---------- freeze ----------
    @Transactional
    public FreezeResult freeze(Long memberId, FreezeRequest r, Authentication a) {
        if (r.days() < minDays || r.days() > maxDays)
            throw new BadRequestException("Durée du gel : de " + minDays + " à " + maxDays + " jours");
        Member m = members.findById(memberId).orElseThrow(() -> new NotFoundException("Membre introuvable"));
        if (m.getStatus() == MemberStatus.FROZEN || freezes.findFirstByMemberIdAndStatus(memberId, ACTIVE).isPresent())
            throw new ConflictException("Ce membre est déjà gelé");
        if (m.getStatus() != MemberStatus.ACTIVE)
            throw new ConflictException("Seul un membre actif peut être gelé");

        LocalDate today = LocalDate.now();
        List<Subscription> live = subs.findByMemberIdAndStatusAndEndDateGreaterThanEqual(memberId, SubscriptionStatus.ACTIVE, today);
        if (live.stream().noneMatch(s -> !s.getStartDate().isAfter(today)))
            throw new ConflictException("Aucun abonnement en cours à geler");

        LocalDate resume = today.plusDays(r.days());
        shift(live, today, r.days());                         // the plan end moves forward now: the expiry job never sees it as finished
        m.setStatus(MemberStatus.FROZEN);                     // set BEFORE cancelling, so the waiting list never re-promotes this member

        MembershipFreeze f = new MembershipFreeze();
        f.setMember(m); f.setStartDate(today); f.setResumeDate(resume); f.setDaysAdded(r.days());
        f.setReason(r.reason() == null || r.reason().isBlank() ? null : r.reason().trim());
        f.setCreatedBy(a.getName()); f.setCreatedAt(LocalDateTime.now()); f.setStatus(ACTIVE);
        freezes.save(f);
        audit.record("MEMBERSHIP_FROZEN", "MEMBER", memberId,
                "Abonnement de " + m.getFirstName() + " " + m.getLastName() + " gelé " + r.days() + " jours (reprise le "
                        + Facts.date(resume) + ")" + (f.getReason() == null ? "" : ", motif : " + f.getReason()));

        int cancelled = cancelBookings(memberId, today, resume, a);
        LocalDate newEnd = latestEnd(live);

        emailQueue.enqueue(EmailType.MEMBERSHIP_FROZEN, m,
                Facts.of("Début du gel", Facts.date(today), "Reprise le", Facts.date(resume),
                        "Nouvelle fin d'abonnement", newEnd == null ? null : Facts.date(newEnd)),
                "FREEZE:" + f.getId());
        log.info("Membre {} gelé {} jours par {}", memberId, r.days(), a.getName());
        return new FreezeResult(row(f), cancelled, newEnd);
    }

    // ---------- resume early ----------
    @Transactional
    public FreezeResult unfreeze(Long memberId, String staff) {
        MembershipFreeze f = freezes.findFirstByMemberIdAndStatus(memberId, ACTIVE)
                .orElseThrow(() -> new NotFoundException("Ce membre n'est pas gelé"));
        LocalDate today = LocalDate.now();
        int unused = (int) Math.max(0, ChronoUnit.DAYS.between(today, f.getResumeDate()));   // days not used: given back
        LocalDate newEnd = end(f, today, unused);
        log.info("Gel du membre {} terminé par {} ({} jours rendus)", memberId, staff, unused);
        return new FreezeResult(row(f), 0, newEnd);
    }

    // ---------- automatic resume (called by the nightly job) ----------
    @Transactional
    public int resumeDue() {
        LocalDate today = LocalDate.now();
        List<MembershipFreeze> due = freezes.findDue(today);
        for (MembershipFreeze f : due) end(f, today, 0);
        if (!due.isEmpty()) log.info("{} gel(s) terminé(s) automatiquement", due.size());
        return due.size();
    }

    @Transactional(readOnly = true)
    public List<FreezeRow> history(Long memberId) {
        return freezes.findByMemberIdOrderByStartDateDesc(memberId).stream().map(this::row).toList();
    }

    // ---------- internals ----------
    private LocalDate end(MembershipFreeze f, LocalDate today, int unused) {
        Member m = f.getMember();
        List<Subscription> live = subs.findByMemberIdAndStatusAndEndDateGreaterThanEqual(m.getId(), SubscriptionStatus.ACTIVE, today);
        if (unused > 0) shift(live, today, -unused);
        f.setEndedOn(today); f.setDaysAdded(f.getDaysAdded() - unused); f.setStatus(ENDED);
        audit.record("MEMBERSHIP_RESUMED", "MEMBER", m.getId(),
                "Abonnement de " + m.getFirstName() + " " + m.getLastName() + " repris"
                        + (unused > 0 ? " en avance (" + unused + " jours rendus)" : ""));
        if (m.getStatus() == MemberStatus.FROZEN) m.setStatus(MemberStatus.ACTIVE);   // a member deactivated meanwhile stays deactivated

        LocalDate newEnd = latestEnd(live);
        emailQueue.enqueue(EmailType.MEMBERSHIP_RESUMED, m,
                Facts.of("Fin d'abonnement", newEnd == null ? null : Facts.date(newEnd)), "RESUME:" + f.getId());
        return newEnd;
    }

    /** Moves every live subscription by 'days' (negative = back). A renewal that starts later moves as a whole. */
    private void shift(List<Subscription> list, LocalDate today, int days) {
        for (Subscription s : list) {
            if (s.getStartDate().isAfter(today)) s.setStartDate(s.getStartDate().plusDays(days));
            s.setEndDate(s.getEndDate().plusDays(days));
        }
    }

    private LocalDate latestEnd(List<Subscription> list) {
        return list.stream().map(Subscription::getEndDate).max(Comparator.naturalOrder()).orElse(null);
    }

    /** Bookings that fall inside the freeze and have not started yet: cancelled, so the places are freed. */
    private int cancelBookings(Long memberId, LocalDate from, LocalDate resume, Authentication a) {
        int n = 0;
        LocalDateTime now = LocalDateTime.now();
        for (Booking b : bookings.mineInRange(memberId, from, resume.minusDays(1), BookingStatus.CANCELLED)) {
            var s = b.getSession();
            if (LocalDateTime.of(s.getSessionDate(), s.getStartTime()).isAfter(now)) {
                bookingService.cancel(b.getId(), a);          // staff path: no 2-hour limit, promotes the waiting list
                n++;
            }
        }
        return n;
    }

    private FreezeRow row(MembershipFreeze f) {
        return new FreezeRow(f.getId(), f.getStartDate(), f.getResumeDate(), f.getEndedOn(),
                f.getDaysAdded(), f.getStatus(), f.getReason(), f.getCreatedBy());
    }
}