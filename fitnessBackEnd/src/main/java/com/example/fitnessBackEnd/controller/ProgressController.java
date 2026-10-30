package com.example.fitnessBackEnd.controller;

import com.example.fitnessBackEnd.dto.MeasurementRequest;
import com.example.fitnessBackEnd.dto.MeasurementResponse;
import com.example.fitnessBackEnd.service.ProgressService;
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
public class ProgressController {
    private final ProgressService service;

    @GetMapping("/members/{id}/measurements") @PreAuthorize("hasAnyRole('ADMIN','COACH')")
    public List<MeasurementResponse> list(@PathVariable Long id, Authentication a) { return service.listFor(id, a); }

    @PostMapping("/members/{id}/measurements") @PreAuthorize("hasAnyRole('ADMIN','COACH')")
    public ResponseEntity<MeasurementResponse> add(@PathVariable Long id,
                                                   @Valid @RequestBody MeasurementRequest r, Authentication a) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.add(id, r, a)); }

    @GetMapping("/progress/me") @PreAuthorize("hasRole('MEMBER')")
    public List<MeasurementResponse> mine(Authentication a) { return service.listMine(a); }

    @PostMapping("/progress/me") @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<MeasurementResponse> addMine(@Valid @RequestBody MeasurementRequest r, Authentication a) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addMine(r, a)); }

    @DeleteMapping("/measurements/{id}") @PreAuthorize("hasAnyRole('ADMIN','COACH','MEMBER')")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication a) {
        service.delete(id, a); return ResponseEntity.noContent().build(); }
}
