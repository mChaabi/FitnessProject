package com.example.fitnessBackEnd.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.LocalTime;

public record SessionRequest(
        @NotBlank @Size(max = 100) String name, @Size(max = 255) String description,
        @NotNull Long coachId, @NotNull @FutureOrPresent LocalDate date, @NotNull LocalTime startTime,
        @Min(15) @Max(240) int durationMinutes, @Min(1) @Max(200) int capacity, @Size(max = 50) String room) {}
