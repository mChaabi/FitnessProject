package com.example.fitnessBackEnd.controller;

import com.example.fitnessBackEnd.dto.DashboardResponse;
import com.example.fitnessBackEnd.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class DashboardController {
    private final DashboardService service;

    @GetMapping("/dashboard") @PreAuthorize("hasRole('ADMIN')")
    public DashboardResponse get() { return service.build(); }
}
