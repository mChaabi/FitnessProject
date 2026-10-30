package com.example.fitnessBackEnd.controller;

import com.example.fitnessBackEnd.dto.ExerciceDTO;
import com.example.fitnessBackEnd.service.ExerciceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/exercices")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class ExerciceController {

    @Autowired
    private ExerciceService exerciceService;

    @GetMapping
    public ResponseEntity<List<ExerciceDTO>> getAll() {
        List<ExerciceDTO> exercices = exerciceService.findAll();
        return ResponseEntity.ok(exercices);
    }

    @PostMapping("/add")
    @PreAuthorize("hasAnyRole('ADMIN','COACH')")
    public ResponseEntity<ExerciceDTO> create(@RequestBody ExerciceDTO exerciceDTO) {
        ExerciceDTO created = exerciceService.save(exerciceDTO);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('ADMIN','COACH')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        exerciceService.delete(id);
        return ResponseEntity.noContent().build(); // Retorna Status 204
    }
}