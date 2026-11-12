package com.example.fitnessBackEnd.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record EnrollmentRow(Long id, Long memberId, String memberName, String email, String phone,
                            Long planId, String planName, BigDecimal price, String coachName,
                            LocalDateTime createdAt) {}