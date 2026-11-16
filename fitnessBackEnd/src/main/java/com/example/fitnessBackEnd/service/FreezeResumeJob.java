package com.example.fitnessBackEnd.service;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FreezeResumeJob {
    private final FreezeService service;

    @EventListener(ApplicationReadyEvent.class)     // also at startup: catches a freeze that ended while the server was off
    @Scheduled(cron = "0 10 0 * * *")               // 00:10, right after the expiry job (00:05)
    public void run() { service.resumeDue(); }
}