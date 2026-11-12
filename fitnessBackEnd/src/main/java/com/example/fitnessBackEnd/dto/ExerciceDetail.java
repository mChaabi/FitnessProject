package com.example.fitnessBackEnd.dto;

import java.util.List;

public record ExerciceDetail(Long id, String nom, String description, Integer duree, String categorie,
                             String image, String objectif, String instructions, String muscles,
                             List<PublicSession> sessions) {}