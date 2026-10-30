package com.example.fitnessBackEnd.entity;

import com.example.fitnessBackEnd.enums.PaymentMethod;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @OneToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "subscription_id", unique = true)
    private Subscription subscription;
    @Column(nullable = false, precision = 10, scale = 2) private BigDecimal amount;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 10) private PaymentMethod method;
    @Column(name = "paid_at", nullable = false) private LocalDateTime paidAt;
    @Column(name = "receipt_number", unique = true, length = 30) private String receiptNumber;
    @Column(name = "received_by", length = 100) private String receivedBy;   // quién cobró
}