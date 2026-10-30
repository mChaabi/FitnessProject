package com.example.fitnessBackEnd.dto;

import java.math.BigDecimal;

public record RevenueSummary(BigDecimal thisMonth, BigDecimal lastMonth) {}