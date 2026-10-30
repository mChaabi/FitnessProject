package com.example.fitnessBackEnd.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ContactDTO(
        Long id,

        @NotBlank(message = "Le nom est requis")
        @Size(min = 2, message = "Le nom est trop court")
        String nom,

        @NotBlank(message = "L'email est requis")
        @Email(message = "Format d'email invalide")
        String email,

        @NotBlank(message = "Le message ne peut pas être vide")
        @Size(min = 10, message = "Le message doit faire au menos 10 caractères")
        String message
) {}