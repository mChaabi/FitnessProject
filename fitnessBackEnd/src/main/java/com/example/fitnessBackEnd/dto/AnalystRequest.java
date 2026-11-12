package com.example.fitnessBackEnd.dto;

import jakarta.validation.constraints.Size;

public record AnalystRequest(@Size(max = 300) String question) {}