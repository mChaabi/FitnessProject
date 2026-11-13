package com.example.fitnessBackEnd.dto;

import java.time.LocalDateTime;

public record FaceStatus(boolean enrolled, LocalDateTime consentAt) {}