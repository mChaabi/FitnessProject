package com.example.fitnessBackEnd.service;

import com.example.fitnessBackEnd.dto.SellRequest;
import com.example.fitnessBackEnd.dto.SubscriptionRow;
import com.example.fitnessBackEnd.entity.Member;
import com.example.fitnessBackEnd.entity.MembershipPlan;
import com.example.fitnessBackEnd.entity.Payment;
import com.example.fitnessBackEnd.entity.Subscription;
import com.example.fitnessBackEnd.enums.MemberStatus;
import com.example.fitnessBackEnd.enums.SubscriptionStatus;
import com.example.fitnessBackEnd.exception.ConflictException;
import com.example.fitnessBackEnd.exception.NotFoundException;
import com.example.fitnessBackEnd.repository.MemberRepository;
import com.example.fitnessBackEnd.repository.PaymentRepository;
import com.example.fitnessBackEnd.repository.PlanRepository;
import com.example.fitnessBackEnd.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final MemberRepository memberRepo;
    private final PlanRepository planRepo;
    private final SubscriptionRepository subRepo;
    private final PaymentRepository payRepo;

    @Transactional
    public SubscriptionRow sell(Long memberId, SellRequest r, String staffEmail) {
        Member member = memberRepo.findById(memberId)
                .orElseThrow(() -> new NotFoundException("Membre introuvable"));
        MembershipPlan plan = planRepo.findById(r.planId())
                .orElseThrow(() -> new NotFoundException("Plan introuvable"));
        if (!plan.isActive()) throw new ConflictException("Ce plan n'est plus disponible");

        LocalDate today = LocalDate.now();
        LocalDate start = subRepo
                .findFirstByMemberIdAndStatusOrderByEndDateDesc(memberId, SubscriptionStatus.ACTIVE)
                .map(Subscription::getEndDate)
                .filter(end -> !end.isBefore(today))      // aún vigente: renueva a continuación
                .map(end -> end.plusDays(1))
                .orElse(today);

        Subscription sub = subRepo.save(Subscription.builder()
                .member(member).plan(plan).startDate(start)
                .endDate(start.plusDays(plan.getDurationDays() - 1L))
                .status(SubscriptionStatus.ACTIVE).build());

        Payment pay = payRepo.save(Payment.builder()
                .subscription(sub).amount(plan.getPrice())           // el precio lo manda el servidor
                .method(r.method()).paidAt(LocalDateTime.now()).receivedBy(staffEmail).build());
        pay.setReceiptNumber(String.format("R-%d-%06d", today.getYear(), pay.getId()));

        member.setStatus(MemberStatus.ACTIVE);
        return row(pay);
    }

    @Transactional(readOnly = true)
    public List<SubscriptionRow> history(Long memberId) {
        return payRepo.findHistory(memberId).stream().map(this::row).toList();
    }

    private SubscriptionRow row(Payment p) {
        Subscription s = p.getSubscription();
        return new SubscriptionRow(s.getId(), s.getPlan().getName(), s.getStartDate(), s.getEndDate(),
                s.getStatus(), p.getId(), p.getReceiptNumber(), p.getAmount());
    }
}
