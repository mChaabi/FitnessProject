package com.example.fitnessBackEnd.dto;

import java.time.LocalDate;

public record MemberOverview(String firstName, String status,          // ACTIVE | EXPIRED | NONE
                             String planName, LocalDate startDate, LocalDate endDate,
                             Long daysLeft, int totalDays,
                             String coachName, String coachSpecialties,
                             String requestedPlan) {}