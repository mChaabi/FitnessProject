package com.example.fitnessBackEnd.controller;

import com.example.fitnessBackEnd.dto.EnrollmentRow;
import com.example.fitnessBackEnd.repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/enrollments")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','RECEPTION')")
public class EnrollmentController {
    private final EnrollmentRepository repo;

    @GetMapping("/pending") @Transactional(readOnly = true)
    public List<EnrollmentRow> pending() {
        return repo.findByStatusDetailed("PENDING").stream().map(r -> new EnrollmentRow(
                r.getId(), r.getMember().getId(),
                r.getMember().getFirstName() + " " + r.getMember().getLastName(),
                r.getMember().getEmail(), r.getMember().getPhone(),
                r.getPlan() == null ? null : r.getPlan().getId(),
                r.getPlan() == null ? null : r.getPlan().getName(),
                r.getPlan() == null ? null : r.getPlan().getPrice(),
                r.getCoach() == null ? null : r.getCoach().getUser().getPrenom() + " " + r.getCoach().getUser().getNom(),
                r.getCreatedAt())).toList();
    }
}