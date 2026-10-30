package com.example.fitnessBackEnd.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LinkAccountRequest(@NotBlank @Email String email) {}