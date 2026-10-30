package com.example.fitnessBackEnd.dto;

import com.example.fitnessBackEnd.enums.BookingStatus;
import com.example.fitnessBackEnd.enums.SessionStatus;

import java.time.LocalDate;
import java.time.LocalTime;

public record SessionResponse(Long id, Long templateId, String name, String description, String coachName,
                              LocalDate date, LocalTime startTime, LocalTime endTime, int capacity, String room, SessionStatus status,
                              int confirmed, int waiting, BookingStatus myStatus, Long myBookingId, Long myPosition, boolean bookable) {}
