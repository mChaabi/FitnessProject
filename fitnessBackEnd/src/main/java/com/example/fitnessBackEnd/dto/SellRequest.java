package com.example.fitnessBackEnd.dto;

import com.example.fitnessBackEnd.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;

public record SellRequest(@NotNull Long planId, @NotNull PaymentMethod method) {}
