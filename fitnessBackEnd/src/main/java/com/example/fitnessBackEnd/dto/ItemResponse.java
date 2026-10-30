package com.example.fitnessBackEnd.dto;

public record ItemResponse(Long id, Long exerciceId, String exerciceName, String categorie,
                           int dayOfWeek, int sets, int reps, int restSeconds, String notes) {}
