package com.example.fitnessBackEnd.dto;

import com.example.fitnessBackEnd.enums.SubscriptionStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SubscriptionRow(Long subscriptionId, String planName, LocalDate startDate, LocalDate endDate,
                              SubscriptionStatus status, Long paymentId, String receiptNumber, BigDecimal amount) {}