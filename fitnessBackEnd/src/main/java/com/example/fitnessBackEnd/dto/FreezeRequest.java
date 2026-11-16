package com.example.fitnessBackEnd.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record FreezeRequest(@Min(1) @Max(365) int days, @Size(max = 255) String reason) {}