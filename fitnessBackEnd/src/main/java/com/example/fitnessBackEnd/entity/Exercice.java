package com.example.fitnessBackEnd.entity;

import com.example.fitnessBackEnd.enums.Categorie;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "exercices")
@Getter             // Génère tous les getters (getId, getNom, getDuree, etc.)
@Setter             // Génère tous les setters (setId, setNom, etc.)
@NoArgsConstructor  // Génère le constructeur sans argument (obligatoire pour JPA)
@AllArgsConstructor // Génère le constructeur avec tous les arguments
@Builder            // Permet de créer des objets facilement : Exercice.builder().nom("...").build()
public class Exercice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;
    private String description;
    private Integer duree;

    @Enumerated(EnumType.STRING)
    private Categorie categorie;

    private String image;
}