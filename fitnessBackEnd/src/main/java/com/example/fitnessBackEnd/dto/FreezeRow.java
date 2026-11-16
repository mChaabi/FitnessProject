package com.example.fitnessBackEnd.dto;

import java.time.LocalDate;

public record FreezeRow(Long id, LocalDate startDate, LocalDate resumeDate, LocalDate endedOn,
                        int daysAdded, String status, String reason, String createdBy) {}