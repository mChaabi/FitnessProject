package com.example.fitnessBackEnd.dto;

import java.time.LocalDateTime;

public record AuditRow(Long id, LocalDateTime createdAt, String actor, String actorRole,
                       String action, String entityType, Long entityId, String summary) {}