package com.example.fitnessBackEnd.entity;

import com.example.fitnessBackEnd.enums.ContactStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "contacts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom est obligatoire")
    @Size(min = 2, max = 50, message = "Le nom doit avoir entre 2 et 50 caractères")
    private String nom;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'email doit être valide")
    private String email;

    @NotBlank(message = "Le message ne peut pas être vide")
    @Size(min = 10, message = "Le message doit contenir au moins 10 caractères")
    @Column(length = 1000)
    private String message;

    @Column(length = 30) private String sujet;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 10)
    private ContactStatus status;

    @Column(name = "created_at", nullable = false) private LocalDateTime createdAt;
    @Column(name = "handled_by", length = 100) private String handledBy;
    @Column(name = "handled_at") private LocalDateTime handledAt;
}