package com.example.fitnessBackEnd.dto;

import com.example.fitnessBackEnd.enums.BookingStatus;

import java.time.LocalDateTime;

public record AttendeeRow(Long bookingId, Long memberId, String memberName, BookingStatus status, LocalDateTime queuedAt) {}
