package com.example.fitnessBackEnd.entity;

import com.example.fitnessBackEnd.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "class_bookings", uniqueConstraints = @UniqueConstraint(columnNames = {"session_id", "member_id"}))
@Getter
@Setter
@NoArgsConstructor
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "session_id") private ClassSession session;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "member_id") private Member member;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 10) private BookingStatus status;
    @Column(name = "queued_at", nullable = false) private LocalDateTime queuedAt;   // orden en la lista de espera
}
