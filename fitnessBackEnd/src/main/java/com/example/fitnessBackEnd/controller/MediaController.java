package com.example.fitnessBackEnd.controller;

import com.example.fitnessBackEnd.exception.NotFoundException;
import com.example.fitnessBackEnd.service.VideoStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;
import java.time.Duration;

@RestController
@RequiredArgsConstructor
public class MediaController {
    private final VideoStorage storage;

    @GetMapping("/media/videos/{name}")
    public ResponseEntity<Resource> file(@PathVariable String name) {
        Path p = storage.resolve(name);
        if (p == null) throw new NotFoundException("Fichier introuvable");
        MediaType type = name.endsWith(".webm") ? MediaType.parseMediaType("video/webm")
                : name.endsWith(".jpg") ? MediaType.IMAGE_JPEG : MediaType.parseMediaType("video/mp4");
        return ResponseEntity.ok().contentType(type)
                .cacheControl(CacheControl.maxAge(Duration.ofDays(7)).cachePublic())
                .body(new FileSystemResource(p));
    }
}