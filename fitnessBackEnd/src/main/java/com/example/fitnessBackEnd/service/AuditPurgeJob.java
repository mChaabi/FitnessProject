package com.example.fitnessBackEnd.service;

import com.example.fitnessBackEnd.repository.AuditRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuditPurgeJob {
    private final AuditRepository repo;
    @Value("${app.audit.retention-days:730}") private int days;

    @Scheduled(cron = "0 30 3 * * *")
    public void purge() {
        int n = repo.purge(LocalDateTime.now().minusDays(days));
        if (n > 0) log.info("{} entrée(s) du journal supprimée(s) (plus de {} jours)", n, days);
    }
}