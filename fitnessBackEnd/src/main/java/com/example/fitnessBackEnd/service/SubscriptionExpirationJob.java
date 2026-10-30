package com.example.fitnessBackEnd.service;

import com.example.fitnessBackEnd.entity.Member;
import com.example.fitnessBackEnd.entity.Subscription;
import com.example.fitnessBackEnd.enums.MemberStatus;
import com.example.fitnessBackEnd.enums.SubscriptionStatus;
import com.example.fitnessBackEnd.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionExpirationJob {

    private final SubscriptionRepository subRepo;

    @EventListener(ApplicationReadyEvent.class)
    @Scheduled(cron = "0 5 0 * * *")          // todos los días a las 00:05
    @Transactional
    public void expire() {
        List<Subscription> expired = subRepo.findByStatusAndEndDateBefore(SubscriptionStatus.ACTIVE, LocalDate.now());
        expired.forEach(s -> s.setStatus(SubscriptionStatus.EXPIRED));

        Set<Member> affected = expired.stream().map(Subscription::getMember).collect(Collectors.toSet());
        for (Member m : affected) {
            boolean stillActive = subRepo.existsByMemberIdAndStatus(m.getId(), SubscriptionStatus.ACTIVE);
            if (!stillActive && m.getStatus() == MemberStatus.ACTIVE) m.setStatus(MemberStatus.EXPIRED);
        }
        if (!expired.isEmpty()) log.info("{} suscripciones caducadas", expired.size());
    }
}