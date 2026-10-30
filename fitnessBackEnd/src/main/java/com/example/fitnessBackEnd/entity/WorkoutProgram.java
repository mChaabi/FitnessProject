package com.example.fitnessBackEnd.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "workout_programs", indexes = {
        @Index(columnList = "member_id"), @Index(columnList = "coach_id")})
@Getter
@Setter
@NoArgsConstructor
public class WorkoutProgram {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "coach_id") private Coach coach;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "member_id") private Member member;

    @Column(nullable = false, length = 100) private String name;
    @Column(length = 255) private String goal;
    @Column(name = "start_date", nullable = false) private LocalDate startDate;
    @Column(nullable = false) private boolean active = true;

    @OneToMany(mappedBy = "program", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("dayOfWeek asc, position asc")
    private List<WorkoutItem> items = new ArrayList<>();
}
