package com.example.fitnessBackEnd.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_log", indexes = {
        @Index(columnList = "created_at"),
        @Index(columnList = "entity_type, entity_id, created_at"),
        @Index(columnList = "actor, created_at")})
@Getter
@Setter
@NoArgsConstructor
public class AuditEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "created_at", nullable = false) private LocalDateTime createdAt;
    @Column(nullable = false, length = 150) private String actor;                 // email, or "system"
    @Column(name = "actor_role", length = 20) private String actorRole;
    @Column(nullable = false, length = 40) private String action;                 // SUBSCRIPTION_SOLD...
    @Column(name = "entity_type", nullable = false, length = 20) private String entityType;   // MEMBER, PLAN, COACH, VIDEO, SESSION
    @Column(name = "entity_id") private Long entityId;
    @Column(nullable = false, length = 300) private String summary;
}