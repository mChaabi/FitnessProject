package com.example.fitnessBackEnd.controller;

import com.example.fitnessBackEnd.dto.AssistantReply;
import com.example.fitnessBackEnd.dto.AssistantRequest;
import com.example.fitnessBackEnd.service.ClassAssistantService;
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
public class AssistantController {
    private final ClassAssistantService service;

    @PostMapping("/class-suggestions") @PreAuthorize("hasRole('MEMBER')")
    public AssistantReply suggest(@Valid @RequestBody AssistantRequest r, Authentication a) {
        return service.suggest(r.message(), a);
    }
}