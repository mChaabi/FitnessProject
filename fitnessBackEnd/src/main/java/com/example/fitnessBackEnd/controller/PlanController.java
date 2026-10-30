package com.example.fitnessBackEnd.controller;

import com.example.fitnessBackEnd.dto.PlanRequest;
import com.example.fitnessBackEnd.dto.PlanResponse;
import com.example.fitnessBackEnd.service.PlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/plans")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class PlanController {
    private final PlanService service;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTION')")
    public List<PlanResponse> active() { return service.activePlans(); }

    @GetMapping("/public")
    public List<PlanResponse> publicPlans() { return service.activePlans(); }

    @GetMapping("/all") @PreAuthorize("hasRole('ADMIN')")
    public List<PlanResponse> all() { return service.all(); }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PlanResponse> create(@Valid @RequestBody PlanRequest r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(r)); }

    @PutMapping("/{id}") @PreAuthorize("hasRole('ADMIN')")
    public PlanResponse update(@PathVariable Long id, @Valid @RequestBody PlanRequest r) { return service.update(id, r); }
}
