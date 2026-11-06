package com.example.fitnessBackEnd.controller;

import com.example.fitnessBackEnd.dto.PublicCoach;
import com.example.fitnessBackEnd.dto.PublicSession;
import com.example.fitnessBackEnd.entity.Coach;
import com.example.fitnessBackEnd.repository.CoachRepository;
import com.example.fitnessBackEnd.service.ClassScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/public") @RequiredArgsConstructor
public class PublicController {
    private final ClassScheduleService schedule;
    private final CoachRepository coaches;

    @GetMapping("/classes")
    public List<PublicSession> classes(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return schedule.publicList(from, to);
    }

    @GetMapping("/coaches") @Transactional(readOnly = true)
    public List<PublicCoach> coaches() {
        return coaches.findAllWithUser().stream().filter(Coach::isActive)
                .map(c -> new PublicCoach(c.getId(),
                        c.getUser().getPrenom() + " " + c.getUser().getNom(), c.getSpecialties(), c.getBio()))
                .toList();
    }
}
