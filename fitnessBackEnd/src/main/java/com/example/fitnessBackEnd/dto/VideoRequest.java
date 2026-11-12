package com.example.fitnessBackEnd.dto;

import com.example.fitnessBackEnd.enums.VideoCategory;
import jakarta.validation.constraints.*;

public record VideoRequest(
        @NotBlank @Size(max = 120) String title,
        @Size(max = 255) String description,
        @NotNull VideoCategory category,
        @NotBlank @Size(max = 255) String url,
        @Min(1) @Max(600) Integer durationMin,
        boolean active) {}
