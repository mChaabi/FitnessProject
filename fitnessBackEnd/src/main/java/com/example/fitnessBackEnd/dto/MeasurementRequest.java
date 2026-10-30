package com.example.fitnessBackEnd.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record MeasurementRequest(
        @NotNull @PastOrPresent LocalDate measuredOn,
        @DecimalMin("20") @DecimalMax("400") Double weightKg,
        @DecimalMin("2")  @DecimalMax("70")  Double bodyFatPercent,
        @DecimalMin("20") @DecimalMax("300") Double waistCm,
        @DecimalMin("20") @DecimalMax("300") Double chestCm,
        @DecimalMin("10") @DecimalMax("100") Double armCm,
        @Size(max = 255) String notes) {}
