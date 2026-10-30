package com.example.fitnessBackEnd.dto;


import java.time.LocalDate;

public record MeasurementResponse(Long id, LocalDate measuredOn, Double weightKg, Double bodyFatPercent,
                                  Double waistCm, Double chestCm, Double armCm, String notes) {}
