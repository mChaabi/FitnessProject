package com.example.fitnessBackEnd.entity;

import com.example.fitnessBackEnd.enums.VideoCategory;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "videos", indexes = @Index(columnList = "category, active"))
@Getter
@Setter
@NoArgsConstructor
public class Video {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 120) private String title;
    @Column(length = 255) private String description;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private VideoCategory category;
    @Column(nullable = false, length = 10) private String type;          // YOUTUBE | FILE
    @Column(nullable = false, length = 255) private String source;       // YouTube ID or /videos/x.mp4
    @Column(name = "duration_min") private Integer durationMin;
    @Column(nullable = false) private boolean active = true;
    @Column(name = "created_at", nullable = false) private LocalDateTime createdAt;
}