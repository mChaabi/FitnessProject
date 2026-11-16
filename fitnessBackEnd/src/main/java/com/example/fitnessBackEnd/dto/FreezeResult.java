package com.example.fitnessBackEnd.dto;

import java.time.LocalDate;

public record FreezeResult(FreezeRow freeze, int cancelledBookings, LocalDate newEndDate) {}