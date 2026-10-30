package com.example.fitnessBackEnd.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "body_measurements",
        uniqueConstraints = @UniqueConstraint(columnNames = {"member_id", "measured_on"}))
@Getter
@Setter
@NoArgsConstructor
public class BodyMeasurement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(name = "measured_on", nullable = false) private LocalDate measuredOn;
    @Column(name = "weight_kg") private Double weightKg;
    @Column(name = "body_fat_percent") private Double bodyFatPercent;
    @Column(name = "waist_cm") private Double waistCm;
    @Column(name = "chest_cm") private Double chestCm;
    @Column(name = "arm_cm") private Double armCm;
    @Column(length = 255) private String notes;
    @Column(name = "recorded_by", length = 100) private String recordedBy;
}
