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
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmailQueue {

    private final OutboxRepository repo;

    @Transactional
    public boolean enqueue(EmailType type, Member m, List<Fact> facts, String dedupKey) {
        if (m.getEmail() == null || m.getEmail().isBlank()) return false;
        if (type.isRespectsOptOut() && m.isEmailOptOut()) return false;       // el miembro se dio de baja
        if (dedupKey != null && repo.existsByDedupKey(dedupKey)) return false; // ya encolado antes

        EmailMessage e = new EmailMessage();
        e.setType(type); e.setMemberId(m.getId()); e.setRecipient(m.getEmail().trim());
        e.setRecipientName(m.getFirstName()); e.setFactsText(Facts.toText(facts));
        e.setDedupKey(dedupKey);
        e.setCreatedAt(LocalDateTime.now()); e.setNextAttemptAt(LocalDateTime.now());
        repo.save(e);
        return true;
    }
}
