package com.example.fitnessBackEnd.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "membership_freezes", indexes = @Index(columnList = "member_id, status"))
@Getter
@Setter
@NoArgsConstructor
public class MembershipFreeze {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "member_id") private Member member;
    @Column(name = "start_date", nullable = false) private LocalDate startDate;
    @Column(name = "resume_date", nullable = false) private LocalDate resumeDate;   // planned return
    @Column(name = "ended_on") private LocalDate endedOn;                            // real return
    @Column(name = "days_added", nullable = false) private int daysAdded;            // days really added to the plan
    @Column(length = 255) private String reason;
    @Column(name = "created_by", nullable = false, length = 100) private String createdBy;
    @Column(name = "created_at", nullable = false) private LocalDateTime createdAt;
    @Column(nullable = false, length = 10) private String status;                    // ACTIVE | ENDED
}