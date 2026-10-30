package com.example.fitnessBackEnd.dto;

import java.time.LocalDate;
import java.util.List;

public record ProgramResponse(Long id, String name, String goal, LocalDate startDate, boolean active,
                              Long memberId, String memberName, String coachName, List<ItemResponse> items) {}
