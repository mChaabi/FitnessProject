package com.example.fitnessBackEnd.dto;

import java.time.LocalDate;

public record ProgramSummary(Long id, String name, LocalDate startDate, boolean active,
                             String memberName, String coachName) {}
