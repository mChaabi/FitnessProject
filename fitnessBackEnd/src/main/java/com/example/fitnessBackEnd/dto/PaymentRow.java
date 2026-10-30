package com.example.fitnessBackEnd.dto;

import com.example.fitnessBackEnd.enums.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentRow(Long id, String receiptNumber, String memberName, String planName,
                         BigDecimal amount, PaymentMethod method, LocalDateTime paidAt) {}
