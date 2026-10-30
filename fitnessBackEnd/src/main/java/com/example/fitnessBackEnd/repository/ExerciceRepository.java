package com.example.fitnessBackEnd.repository;

import com.example.fitnessBackEnd.entity.Exercice;
import com.example.fitnessBackEnd.enums.Categorie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExerciceRepository extends JpaRepository<Exercice, Long> {
    // Puedes agregar búsquedas personalizadas si quieres
    List<Exercice> findByCategorie(Categorie categorie);
}