package com.example.fitnessBackEnd.service;

import com.example.fitnessBackEnd.dto.Fact;
import com.example.fitnessBackEnd.entity.EmailMessage;
import com.example.fitnessBackEnd.entity.Member;
import com.example.fitnessBackEnd.enums.EmailType;
import com.example.fitnessBackEnd.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class EmailQueue {

    /** Marketing-style emails: a member receives at most one of them every 3 days. */
    private static final Set<EmailType> CAMPAIGNS =
            EnumSet.of(EmailType.SUBSCRIPTION_EXPIRING, EmailType.CHURN_RISK, EmailType.PAYMENT_REMINDER);
    private static final int CAMPAIGN_GAP_DAYS = 3;

    private final OutboxRepository repo;

    @Transactional
    public boolean enqueue(EmailType type, Member m, List<Fact> facts, String dedupKey) {
        if (m.getEmail() == null || m.getEmail().isBlank()) return false;
        if (type.isRespectsOptOut() && m.isEmailOptOut()) return false;       // the member unsubscribed
        if (dedupKey != null && repo.existsByDedupKey(dedupKey)) return false; // already queued for this event
        if (CAMPAIGNS.contains(type) && repo.existsByMemberIdAndTypeInAndCreatedAtAfter(
                m.getId(), CAMPAIGNS, LocalDateTime.now().minusDays(CAMPAIGN_GAP_DAYS)))
            return false;                      // too soon: nothing is stored, so the daily job tries again tomorrow

        EmailMessage e = new EmailMessage();
        e.setType(type); e.setMemberId(m.getId()); e.setRecipient(m.getEmail().trim());
        e.setRecipientName(m.getFirstName()); e.setFactsText(Facts.toText(facts));
        e.setDedupKey(dedupKey);
        e.setCreatedAt(LocalDateTime.now()); e.setNextAttemptAt(LocalDateTime.now());
        repo.save(e);
        return true;
    }
}