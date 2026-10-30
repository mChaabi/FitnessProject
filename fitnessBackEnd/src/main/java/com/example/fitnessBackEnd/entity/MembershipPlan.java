package com.example.fitnessBackEnd.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "membership_plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MembershipPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true, length = 80) private String name;
    @Column(nullable = false, precision = 10, scale = 2) private BigDecimal price;
    @Column(name = "duration_days", nullable = false) private int durationDays;
    @Column(length = 255) private String description;
    @Column(nullable = false) private boolean active = true;
}