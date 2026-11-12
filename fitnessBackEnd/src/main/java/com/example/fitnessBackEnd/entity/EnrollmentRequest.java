package com.example.fitnessBackEnd.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "enrollment_requests", indexes = @Index(columnList = "member_id, status"))
@Getter
@Setter
@NoArgsConstructor
public class EnrollmentRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "member_id") private Member member;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "plan_id") private MembershipPlan plan;   // wished plan
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "coach_id") private Coach coach;          // wished coach
    @Column(nullable = false, length = 10) private String status;                                  // PENDING, DONE
    @Column(name = "created_at", nullable = false) private LocalDateTime createdAt;
}
