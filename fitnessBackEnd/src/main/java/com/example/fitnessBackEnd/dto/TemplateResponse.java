package com.example.fitnessBackEnd.dto;

import java.time.LocalTime;

public record TemplateResponse(Long id, String name, String description, Long coachId, String coachName,
                               int dayOfWeek, LocalTime startTime, int durationMinutes, int capacity, String room, boolean active) {}
