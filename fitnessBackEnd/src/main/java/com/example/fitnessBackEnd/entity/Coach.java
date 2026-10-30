package com.example.fitnessBackEnd.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "coaches")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coach {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @Column(length = 150) private String specialties;   // "Musculation, Cardio"
    @Column(length = 1000) private String bio;
    @Column(nullable = false) private boolean active;
}