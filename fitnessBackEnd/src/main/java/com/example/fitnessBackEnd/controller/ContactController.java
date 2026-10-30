package com.example.fitnessBackEnd.controller;

import com.example.fitnessBackEnd.dto.ContactDTO;
import com.example.fitnessBackEnd.service.ContactService;
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

    @PostMapping("/send")
    public ResponseEntity<ContactDTO> sendContact(@Valid @RequestBody ContactDTO dto) {
        return new ResponseEntity<>(contactService.saveMessage(dto), HttpStatus.CREATED);
    }
}