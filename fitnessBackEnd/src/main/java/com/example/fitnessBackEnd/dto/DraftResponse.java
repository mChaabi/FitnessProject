package com.example.fitnessBackEnd.dto;

import java.time.LocalDate;
import java.util.List;

public record DraftResponse(String runId, boolean ready, int attempts, List<String> errors,
                            Long memberId, String name, String goal, LocalDate startDate, List<DraftItem> items) {}
