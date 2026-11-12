package com.example.fitnessBackEnd.dto;

import java.util.List;

/** ai = false when the keyword fallback answered. */
public record AssistantReply(String reply, List<Long> sessionIds, boolean ai) {}