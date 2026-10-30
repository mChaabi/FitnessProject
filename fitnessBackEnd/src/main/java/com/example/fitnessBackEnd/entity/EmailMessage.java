package com.example.fitnessBackEnd.entity;

import com.example.fitnessBackEnd.enums.EmailStatus;
import com.example.fitnessBackEnd.enums.EmailType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_outbox",
        uniqueConstraints = @UniqueConstraint(name = "uk_outbox_dedup", columnNames = "dedup_key"),
        indexes = @Index(columnList = "status, next_attempt_at"))
@Getter
@Setter
@NoArgsConstructor
public class EmailMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30) private EmailType type;
    @Column(name = "member_id", nullable = false) private Long memberId;
    @Column(nullable = false, length = 150) private String recipient;
    @Column(name = "recipient_name", length = 100) private String recipientName;   // solo el nombre de pila
    @Column(name = "facts_text", columnDefinition = "TEXT") private String factsText;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 10) private EmailStatus status = EmailStatus.PENDING;
    @Column(nullable = false) private int attempts;
    @Column(name = "next_attempt_at", nullable = false) private LocalDateTime nextAttemptAt;
    @Column(name = "claimed_at") private LocalDateTime claimedAt;
    @Column(name = "created_at", nullable = false) private LocalDateTime createdAt;
    @Column(name = "sent_at") private LocalDateTime sentAt;
    @Column(name = "ai_generated") private Boolean aiGenerated;
    @Column(name = "last_error", length = 500) private String lastError;
    @Column(name = "dedup_key", length = 120) private String dedupKey;
}
