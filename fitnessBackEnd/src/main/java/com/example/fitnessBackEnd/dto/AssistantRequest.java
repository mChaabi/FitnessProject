package com.example.fitnessBackEnd.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AssistantRequest(@NotBlank @Size(min = 3, max = 300) String message) {}