package com.example.fitnessBackEnd.controller;

import com.example.fitnessBackEnd.dto.ContactDTO;
import com.example.fitnessBackEnd.service.ContactService;
import com.example.fitnessBackEnd.service.LoginRateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/contacts")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class ContactController {

    private final ContactService contactService;
    private final LoginRateLimiter limiter;

    @PostMapping("/send")
    public ResponseEntity<ContactDTO> sendContact(@Valid @RequestBody ContactDTO dto, HttpServletRequest req) {
        String key = "contact|" + req.getRemoteAddr();
        limiter.check(key);                  // 429 when the address is locked
        limiter.failure(key, 5);             // 5 messages per 15 minutes per address
        return new ResponseEntity<>(contactService.saveMessage(dto), HttpStatus.CREATED);
    }
}