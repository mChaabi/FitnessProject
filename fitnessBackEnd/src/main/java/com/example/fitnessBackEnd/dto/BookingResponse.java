package com.example.fitnessBackEnd.dto;

import com.example.fitnessBackEnd.enums.BookingStatus;

public record BookingResponse(Long bookingId, BookingStatus status, Long position) {}
