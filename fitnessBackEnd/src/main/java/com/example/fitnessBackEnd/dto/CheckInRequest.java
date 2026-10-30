package com.example.fitnessBackEnd.dto;

import jakarta.validation.constraints.Size;

public record CheckInRequest(@Size(max = 64) String qrCode, Long memberId) {}