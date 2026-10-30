package com.example.fitnessBackEnd.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

@Entity
@Table(name = "class_templates")
@Getter
@Setter
@NoArgsConstructor
public class ClassTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 100) private String name;
    @Column(length = 255) private String description;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "coach_id") private Coach coach;
    @Column(name = "day_of_week", nullable = false) private int dayOfWeek;      // 1 = lunes ... 7 = domingo
    @Column(name = "start_time", nullable = false) private LocalTime startTime;
    @Column(name = "duration_minutes", nullable = false) private int durationMinutes;
    @Column(nullable = false) private int capacity;
    @Column(length = 50) private String room;
    @Column(nullable = false) private boolean active = true;
}
