package com.example.fitnessBackEnd.controller;

import com.example.fitnessBackEnd.dto.SellRequest;
import com.example.fitnessBackEnd.dto.SubscriptionRow;
import com.example.fitnessBackEnd.service.SubscriptionService;
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
public class SubscriptionController {
    private final SubscriptionService service;

    @PostMapping("/members/{id}/subscriptions")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTION')")
    public ResponseEntity<SubscriptionRow> sell(@PathVariable Long id, @Valid @RequestBody SellRequest r,
                                                Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.sell(id, r, auth.getName()));
    }

    @GetMapping("/members/{id}/subscriptions") @PreAuthorize("hasAnyRole('ADMIN','RECEPTION')")
    public List<SubscriptionRow> history(@PathVariable Long id) { return service.history(id); }
}
