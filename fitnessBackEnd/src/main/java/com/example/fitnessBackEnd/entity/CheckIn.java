package com.example.fitnessBackEnd.entity;

import com.example.fitnessBackEnd.enums.CheckInOutcome;
import com.example.fitnessBackEnd.enums.DenyReason;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "check_ins", indexes = @Index(columnList = "member_id, checked_at"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckIn {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(name = "checked_at", nullable = false) private LocalDateTime checkedAt;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 10) private CheckInOutcome outcome;
    @Enumerated(EnumType.STRING) @Column(length = 20) private DenyReason reason;   // null si ALLOWED

    @Column(name = "performed_by", length = 100) private String performedBy;       // quién escaneó
}
