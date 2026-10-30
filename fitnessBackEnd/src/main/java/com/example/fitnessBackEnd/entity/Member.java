package com.example.fitnessBackEnd.entity;

import com.example.fitnessBackEnd.enums.MemberStatus;
import com.example.fitnessBackEnd.enums.Sex;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "members", indexes = {
        @Index(columnList = "email"), @Index(columnList = "last_name")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false, length = 50) private String firstName;
    @Column(name = "last_name",  nullable = false, length = 50) private String lastName;

    @Column(nullable = false, unique = true, length = 100) private String email;
    @Column(length = 20) private String phone;

    @Column(name = "birth_date") private LocalDate birthDate;
    @Enumerated(EnumType.STRING) @Column(length = 10) private Sex sex;

    @Column(length = 255) private String goal;           // "perdre du poids", "prise de masse"...

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 10)
    private MemberStatus status;

    /** Código del QR. Aleatorio e independiente del id, para que no se pueda adivinar. */
    @Column(name = "qr_code", nullable = false, unique = true, length = 36, updatable = false)
    private String qrCode;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coach_id")
    private Coach coach;// opcional

    @Column(name = "joined_at", nullable = false, updatable = false)
    private LocalDate joinedAt;

    @Column(name = "email_opt_out", nullable = false)
    private boolean emailOptOut;

    @PrePersist
    void onCreate() {
        if (qrCode == null) qrCode = UUID.randomUUID().toString();
        if (joinedAt == null) joinedAt = LocalDate.now();
        if (status == null) status = MemberStatus.INACTIVE;   // pasa a ACTIVE al contratar un plan (Fase 5)
    }
}