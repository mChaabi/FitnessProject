package com.example.fitnessBackEnd.controller;

import com.example.fitnessBackEnd.exception.BadRequestException;
import com.example.fitnessBackEnd.repository.MemberRepository;
import com.example.fitnessBackEnd.service.UnsubscribeTokens;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class UnsubscribeController {

    private final UnsubscribeTokens tokens;
    private final MemberRepository members;

    @PostMapping("/unsubscribe")
    public ResponseEntity<Void> unsubscribe(@RequestParam("t") String token) {
        long id = tokens.verify(token).orElseThrow(() -> new BadRequestException("Lien invalide"));
        members.optOut(id);
        return ResponseEntity.noContent().build();      // idempotente: repetirlo no hace daño
    }
}
