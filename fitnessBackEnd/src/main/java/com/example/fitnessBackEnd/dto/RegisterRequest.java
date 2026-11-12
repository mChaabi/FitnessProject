package com.example.fitnessBackEnd.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Size(max = 50) String nom,
        @NotBlank @Size(max = 50) String prenom,
        @NotBlank @Email @Size(max = 100) String email,
        @NotBlank @Size(min = 8, max = 72) String motDePasse,   // 72 = BCrypt limit
        @Pattern(regexp = "^[+0-9 ()-]{6,20}$", message = "Téléphone invalide") String phone,
        Long planId,
        Long coachId) {}