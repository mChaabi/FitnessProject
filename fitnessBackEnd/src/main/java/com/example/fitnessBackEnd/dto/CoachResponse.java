package com.example.fitnessBackEnd.dto;

public record CoachResponse(Long id, String nom, String prenom, String email,
                            String specialties, String bio, boolean active, long clients) {}

