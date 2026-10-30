package com.example.fitnessBackEnd.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record ProgramRequest(
        @NotNull Long memberId,
        @NotBlank @Size(max = 100) String name,
        @Size(max = 255) String goal,
        @NotNull LocalDate startDate,
        boolean active,
        @NotEmpty @Size(max = 60) List<@Valid ItemRequest> items) {}
