package com.example.fitnessBackEnd.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ContactDTO(
        Long id,

        @NotBlank(message = "Le nom est requis")
        @Size(min = 2, max = 50, message = "Le nom doit avoir entre 2 et 50 caractères")
        String nom,

        @NotBlank(message = "L'email est requis")
        @Email(message = "Format d'email invalide")
        @Size(max = 255)
        String email,

        @Size(max = 30) String sujet,

        @NotBlank(message = "Le message ne peut pas être vide")
        @Size(min = 10, max = 1000, message = "Le message doit faire entre 10 et 1000 caractères")
        String message,

        String website          // honeypot: real visitors never fill it
) {}