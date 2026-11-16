package com.example.fitnessBackEnd.dto;

import com.example.fitnessBackEnd.enums.ContactStatus;

import java.time.LocalDateTime;

public record ContactRow(Long id, String nom, String email, String sujet, String message,
                         ContactStatus status, LocalDateTime createdAt, String handledBy, LocalDateTime handledAt) {}