package com.example.fitnessBackEnd.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CoachRequest(
        @NotBlank @Size(max = 50) String nom,
        @NotBlank @Size(max = 50) String prenom,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, max = 72) String motDePasse,
        @Size(max = 150) String specialties,
        @Size(max = 1000) String bio) {}
