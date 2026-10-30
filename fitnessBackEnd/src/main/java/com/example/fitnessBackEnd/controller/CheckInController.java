package com.example.fitnessBackEnd.controller;

import com.example.fitnessBackEnd.dto.CheckInRequest;
import com.example.fitnessBackEnd.dto.CheckInResult;
import com.example.fitnessBackEnd.dto.CheckInRow;
import com.example.fitnessBackEnd.dto.TodayStats;
import com.example.fitnessBackEnd.service.CheckInService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/checkins")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','RECEPTION')")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class CheckInController {

    private final CheckInService service;

    @PostMapping
    public CheckInResult check(@Valid @RequestBody CheckInRequest r, Authentication auth) {
        return service.check(r, auth.getName());
    }

    @GetMapping("/today") public List<CheckInRow> today() { return service.today(); }
    @GetMapping("/stats") public TodayStats stats() { return service.stats(); }
}
