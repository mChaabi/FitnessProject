package com.example.fitnessBackEnd.dto;

public record DraftItem(Long exerciceId, int dayOfWeek, int sets, int reps, int restSeconds, String notes) {}
