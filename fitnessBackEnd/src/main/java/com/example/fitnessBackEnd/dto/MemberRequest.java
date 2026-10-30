package com.example.fitnessBackEnd.dto;

import com.example.fitnessBackEnd.enums.Sex;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record MemberRequest(
        @NotBlank @Size(max = 50) String firstName,
        @NotBlank @Size(max = 50) String lastName,
        @NotBlank @Email @Size(max = 100) String email,
        @Pattern(regexp = "^[+0-9 ()-]{6,20}$", message = "Téléphone invalide") String phone,
        @Past LocalDate birthDate,
        Sex sex,
        @Size(max = 255) String goal
) {}