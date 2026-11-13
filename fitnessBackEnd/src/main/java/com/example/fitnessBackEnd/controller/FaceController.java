package com.example.fitnessBackEnd.controller;

import com.example.fitnessBackEnd.dto.FaceMatch;
import com.example.fitnessBackEnd.dto.FaceStatus;
import com.example.fitnessBackEnd.service.FaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/face")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','RECEPTION')")
public class FaceController {

    private final FaceService service;

    @PostMapping(value = "/members/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public FaceStatus enroll(@PathVariable Long id, @RequestParam boolean consent,
                             @RequestPart("photo") MultipartFile photo, Authentication a) {
        return service.enroll(id, consent, photo, a.getName());
    }

    @GetMapping("/members/{id}")
    public FaceStatus status(@PathVariable Long id) { return service.status(id); }

    @DeleteMapping("/members/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication a) {
        service.delete(id, a.getName());
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/identify", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public FaceMatch identify(@RequestPart("photo") MultipartFile photo) { return service.identify(photo); }
}