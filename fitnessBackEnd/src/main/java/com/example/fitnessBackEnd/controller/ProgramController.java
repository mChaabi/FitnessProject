package com.example.fitnessBackEnd.controller;

import com.example.fitnessBackEnd.dto.ProgramRequest;
import com.example.fitnessBackEnd.dto.ProgramResponse;
import com.example.fitnessBackEnd.dto.ProgramSummary;
import com.example.fitnessBackEnd.service.ProgramService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class ProgramController {
    private final ProgramService service;

    @PostMapping("/programs") @PreAuthorize("hasAnyRole('ADMIN','COACH')")
    public ResponseEntity<ProgramResponse> create(@Valid @RequestBody ProgramRequest r, Authentication a) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(r, a)); }

    @PutMapping("/programs/{id}") @PreAuthorize("hasAnyRole('ADMIN','COACH')")
    public ProgramResponse update(@PathVariable Long id, @Valid @RequestBody ProgramRequest r, Authentication a) {
        return service.update(id, r, a); }

    @DeleteMapping("/programs/{id}") @PreAuthorize("hasAnyRole('ADMIN','COACH')")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication a) {
        service.delete(id, a); return ResponseEntity.noContent().build(); }

    @GetMapping("/programs/me") @PreAuthorize("hasRole('MEMBER')")
    public List<ProgramResponse> mine(Authentication a) { return service.mine(a.getName()); }

    @GetMapping("/programs/{id}")
    public ProgramResponse get(@PathVariable Long id, Authentication a) { return service.get(id, a); }

    @GetMapping("/members/{id}/programs") @PreAuthorize("hasAnyRole('ADMIN','COACH')")
    public List<ProgramSummary> forMember(@PathVariable Long id, Authentication a) { return service.forMember(id, a); }
}