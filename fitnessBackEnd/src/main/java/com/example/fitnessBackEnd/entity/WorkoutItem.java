package com.example.fitnessBackEnd.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "workout_items")
@Getter
@Setter
@NoArgsConstructor
public class WorkoutItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "program_id") private WorkoutProgram program;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "exercice_id") private Exercice exercice;

    @Column(name = "day_of_week", nullable = false) private int dayOfWeek;   // 1 = lundi ... 7 = dimanche
    @Column(nullable = false) private int position;                          // orden dentro del día
    @Column(nullable = false) private int sets;
    @Column(nullable = false) private int reps;
    @Column(name = "rest_seconds", nullable = false) private int restSeconds;
    @Column(length = 255) private String notes;
}