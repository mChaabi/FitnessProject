package com.example.fitnessBackEnd.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "face_templates")
@Getter
@Setter
@NoArgsConstructor
public class FaceTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @OneToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "member_id") private Member member;
    @Lob @Column(nullable = false) private byte[] embedding;                 // 128 floats, 512 bytes
    @Column(name = "consent_at", nullable = false) private LocalDateTime consentAt;
    @Column(name = "recorded_by", nullable = false, length = 100) private String recordedBy;
    @Column(name = "created_at", nullable = false) private LocalDateTime createdAt;
}