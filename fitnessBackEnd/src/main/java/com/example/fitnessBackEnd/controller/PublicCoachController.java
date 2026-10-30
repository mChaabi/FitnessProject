package com.example.fitnessBackEnd.controller;

import com.example.fitnessBackEnd.entity.Coach;
import com.example.fitnessBackEnd.repository.CoachRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/coaches")
@RequiredArgsConstructor
public class PublicCoachController {
    public record PublicCoach(Long id, String prenom, String nom) {}

    private final CoachRepository repo;

    @GetMapping("/public")
    public List<PublicCoach> publicCoaches() {
        return repo.findAll().stream()
                .filter(Coach::isActive)
                .map(c -> new PublicCoach(
                        c.getId(),
                        c.getUser().getPrenom(), // Fetched from the User entity
                        c.getUser().getNom()     // Fetched from the User entity
                ))
                .toList();
    }
}