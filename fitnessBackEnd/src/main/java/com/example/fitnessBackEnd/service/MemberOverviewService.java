package com.example.fitnessBackEnd.service;

import com.example.fitnessBackEnd.dto.MemberOverview;
import com.example.fitnessBackEnd.entity.Coach;
import com.example.fitnessBackEnd.entity.Member;
import com.example.fitnessBackEnd.entity.Subscription;
import com.example.fitnessBackEnd.enums.SubscriptionStatus;
import com.example.fitnessBackEnd.exception.NotFoundException;
import com.example.fitnessBackEnd.repository.EnrollmentRepository;
import com.example.fitnessBackEnd.repository.MemberRepository;
import com.example.fitnessBackEnd.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class MemberOverviewService {

    private final MemberRepository members;
    private final SubscriptionRepository subs;
    private final EnrollmentRepository requests;

    @Transactional(readOnly = true)          // lazy relations (coach, plan) are read inside the transaction
    public MemberOverview mine(String email) {
        Member m = members.findByUserEmail(email)
                .orElseThrow(() -> new NotFoundException("Aucun membre lié à votre compte"));
        LocalDate today = LocalDate.now();

        Subscription s = subs.findCovering(m.getId(), SubscriptionStatus.ACTIVE, today)
                .stream().findFirst().orElse(null);
        String status = s != null ? "ACTIVE" : subs.existsByMemberId(m.getId()) ? "EXPIRED" : "NONE";

        Coach c = m.getCoach();
        String requested = requests.findFirstByMemberIdAndStatusOrderByCreatedAtDesc(m.getId(), "PENDING")
                .map(r -> r.getPlan() == null ? null : r.getPlan().getName()).orElse(null);

        return new MemberOverview(m.getFirstName(), status,
                s == null ? null : s.getPlan().getName(),
                s == null ? null : s.getStartDate(), s == null ? null : s.getEndDate(),
                s == null ? null : ChronoUnit.DAYS.between(today, s.getEndDate()),
                s == null ? 0 : (int) (ChronoUnit.DAYS.between(s.getStartDate(), s.getEndDate()) + 1),
                c == null ? null : c.getUser().getPrenom() + " " + c.getUser().getNom(),
                c == null ? null : c.getSpecialties(),
                requested);
    }
}