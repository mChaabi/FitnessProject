package com.example.fitnessBackEnd.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record PlanRequest(
        @NotBlank @Size(max = 80) String name,
        @NotNull @DecimalMin("0.01") @Digits(integer = 8, fraction = 2) BigDecimal price,
        @Min(1) @Max(3650) int durationDays,
        @Size(max = 255) String description,
        boolean active) {}
