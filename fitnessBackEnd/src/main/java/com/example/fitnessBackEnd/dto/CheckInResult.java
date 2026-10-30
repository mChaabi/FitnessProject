package com.example.fitnessBackEnd.dto;

import com.example.fitnessBackEnd.enums.CheckInOutcome;
import com.example.fitnessBackEnd.enums.DenyReason;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record CheckInResult(CheckInOutcome outcome, DenyReason reason, String message,
                            Long memberId, String memberName, String planName,
                            LocalDate endDate, Long daysLeft, boolean expiringSoon, LocalDateTime at) {}
