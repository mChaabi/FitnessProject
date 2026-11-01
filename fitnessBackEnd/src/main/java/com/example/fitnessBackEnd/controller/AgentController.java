package com.example.fitnessBackEnd.controller;

import com.example.fitnessBackEnd.dto.DraftRequest;
import com.example.fitnessBackEnd.dto.DraftResponse;
import com.example.fitnessBackEnd.service.ProgramDraftService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/agents") @RequiredArgsConstructor
public class AgentController {

    private final ProgramDraftService drafts;

    @PostMapping("/program-drafts") @PreAuthorize("hasAnyRole('ADMIN','COACH')")
    public DraftResponse draft(@Valid @RequestBody DraftRequest r, Authentication a) {
        return drafts.draft(r, a);
    }
}