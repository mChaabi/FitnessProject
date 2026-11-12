package com.example.fitnessBackEnd.controller;

import com.example.fitnessBackEnd.dto.AnalysisReply;
import com.example.fitnessBackEnd.dto.AnalystRequest;
import com.example.fitnessBackEnd.service.AnalystService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/agents")
@RequiredArgsConstructor
public class AnalystController {
    private final AnalystService service;

    @PostMapping("/analyst") @PreAuthorize("hasRole('ADMIN')")
    public AnalysisReply analyze(@Valid @RequestBody AnalystRequest r, Authentication a) {
        return service.analyze(r.question(), a);
    }
}