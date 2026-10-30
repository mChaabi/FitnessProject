package com.example.fitnessBackEnd.dto;

import com.example.fitnessBackEnd.enums.CheckInOutcome;
import com.example.fitnessBackEnd.enums.DenyReason;

import java.time.LocalDateTime;

public record CheckInRow(Long id, Long memberId, String memberName, LocalDateTime checkedAt,
                         CheckInOutcome outcome, DenyReason reason) {}