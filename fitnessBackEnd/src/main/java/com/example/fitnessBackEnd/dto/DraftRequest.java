package com.example.fitnessBackEnd.dto;

import com.example.fitnessBackEnd.enums.Level;
import jakarta.validation.constraints.*;

import java.util.Set;

public record DraftRequest(
        @NotNull Long memberId,
        @NotNull Level level,
        @NotEmpty @Size(max = 6) Set<@Min(1) @Max(7) Integer> days,
        @Min(20) @Max(120) int minutesPerSession) {}
