package com.example.fitnessBackEnd.dto;

import com.example.fitnessBackEnd.enums.Role;

public record UserDTO(Long id, String nom, String prenom, String email, Role role) {}
