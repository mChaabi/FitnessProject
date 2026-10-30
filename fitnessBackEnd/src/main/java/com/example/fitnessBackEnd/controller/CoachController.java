package com.example.fitnessBackEnd.controller;

import com.example.fitnessBackEnd.dto.*;
import com.example.fitnessBackEnd.service.CoachService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class CoachController {
    private final CoachService service;

    @GetMapping("/coaches") @PreAuthorize("hasAnyRole('ADMIN','RECEPTION')")
    public List<CoachResponse> list() { return service.list(); }

    @PostMapping("/coaches") @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CoachResponse> create(@Valid @RequestBody CoachRequest r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(r)); }

    @PatchMapping("/coaches/{id}/active") @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> active(@PathVariable Long id, @RequestParam boolean value) {
        service.setActive(id, value); return ResponseEntity.noContent().build(); }

    @PutMapping("/members/{id}/coach") @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> assign(@PathVariable Long id, @RequestBody AssignCoachRequest r) {
        service.assign(id, r.coachId()); return ResponseEntity.noContent().build(); }

    @PostMapping("/members/{id}/account") @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> link(@PathVariable Long id, @Valid @RequestBody LinkAccountRequest r) {
        service.linkAccount(id, r.email()); return ResponseEntity.noContent().build(); }

    @GetMapping("/coach/clients") @PreAuthorize("hasRole('COACH')")
    public Page<MemberResponse> clients(Authentication auth, @RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "20") int size) {
        return service.clientsOf(auth.getName(), page, size); }
}