package com.example.fitnessBackEnd.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ItemRequest(
        @NotNull Long exerciceId,
        @Min(1) @Max(7) int dayOfWeek,
        @Min(1) @Max(20) int sets,
        @Min(1) @Max(100) int reps,
        @Min(0) @Max(600) int restSeconds,
        @Size(max = 255) String notes) {}
