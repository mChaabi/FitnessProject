package com.example.fitnessBackEnd.dto;

import java.math.BigDecimal;

public record PlanResponse(Long id, String name, BigDecimal price, int durationDays,
                           String description, boolean active) {}
