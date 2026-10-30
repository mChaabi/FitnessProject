package com.example.fitnessBackEnd.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Size(max = 50) String nom,
        @NotBlank @Size(max = 50) String prenom,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, max = 72) String motDePasse   // 72 = límite real de BCrypt
) {}
