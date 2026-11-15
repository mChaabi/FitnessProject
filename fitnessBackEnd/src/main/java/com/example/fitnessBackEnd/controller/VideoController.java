package com.example.fitnessBackEnd.controller;

import com.example.fitnessBackEnd.dto.VideoRequest;
import com.example.fitnessBackEnd.dto.VideoResponse;
import com.example.fitnessBackEnd.enums.VideoCategory;
import com.example.fitnessBackEnd.service.VideoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class VideoController {
    private final VideoService service;

    /** Any logged-in user (members see the library). Anonymous visitors get 401. */
    @GetMapping("/videos")
    public List<VideoResponse> list() { return service.published(); }

    @GetMapping("/admin/videos") @PreAuthorize("hasRole('ADMIN')")
    public List<VideoResponse> all() { return service.all(); }

    @PostMapping("/admin/videos") @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VideoResponse> create(@Valid @RequestBody VideoRequest r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(r)); }

    @PutMapping("/admin/videos/{id}") @PreAuthorize("hasRole('ADMIN')")
    public VideoResponse update(@PathVariable Long id, @Valid @RequestBody VideoRequest r) { return service.update(id, r); }

    @DeleteMapping("/admin/videos/{id}") @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) { service.delete(id); return ResponseEntity.noContent().build(); }

    @PostMapping(value = "/admin/videos/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VideoResponse> upload(@RequestPart("file") MultipartFile file,
                                                @RequestPart(value = "thumb", required = false) MultipartFile thumb,
                                                @RequestParam String title,
                                                @RequestParam(required = false) String description,
                                                @RequestParam VideoCategory category,
                                                @RequestParam(required = false) Integer durationMin,
                                                @RequestParam(defaultValue = "true") boolean active) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.upload(file, thumb, title, description, category, durationMin, active));
    }
}