package com.example.fitnessBackEnd.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record PublicSession(Long id, String name, String description, String coachName,
                            LocalDate date, LocalTime startTime, LocalTime endTime, String room, int capacity, int freeSpots) {}
