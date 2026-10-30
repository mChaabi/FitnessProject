package com.example.fitnessBackEnd.entity;

import com.example.fitnessBackEnd.enums.SessionStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "class_sessions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"template_id", "session_date"}),
        indexes = @Index(columnList = "session_date"))
@Getter
@Setter
@NoArgsConstructor
public class ClassSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "template_id")        // null = sesión suelta
    private ClassTemplate template;

    @Column(nullable = false, length = 100) private String name;
    @Column(length = 255) private String description;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "coach_id") private Coach coach;
    @Column(name = "session_date", nullable = false) private LocalDate sessionDate;
    @Column(name = "start_time", nullable = false) private LocalTime startTime;
    @Column(name = "end_time", nullable = false) private LocalTime endTime;
    @Column(nullable = false) private int capacity;
    @Column(length = 50) private String room;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 10) private SessionStatus status = SessionStatus.SCHEDULED;
}
