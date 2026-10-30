package com.example.fitnessBackEnd.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class ClassGenerationJob {
    private final ClassScheduleService service;
    @Value("${app.classes.generate-days:28}") private int horizon;

    @EventListener(ApplicationReadyEvent.class)
    @Scheduled(cron = "0 30 1 * * *")
    public void run() { service.generate(LocalDate.now(), LocalDate.now().plusDays(horizon)); }
}
