package com.example.fitnessBackEnd.dto;

public record ExerciceDTO(
        Long id,
        String nom,
        String description,
        Integer duree,
        String categorie,
        String image
) {}
