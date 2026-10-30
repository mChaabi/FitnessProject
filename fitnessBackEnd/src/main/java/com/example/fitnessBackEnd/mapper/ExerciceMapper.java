package com.example.fitnessBackEnd.mapper;

import com.example.fitnessBackEnd.dto.ExerciceDTO;
import com.example.fitnessBackEnd.entity.Exercice;
import com.example.fitnessBackEnd.enums.Categorie;
import org.springframework.stereotype.Component;

@Component
public class ExerciceMapper {

    public ExerciceDTO toDTO(Exercice entity) {
        return new ExerciceDTO(
                entity.getId(),
                entity.getNom(),
                entity.getDescription(),
                entity.getDuree(),
                entity.getCategorie().name(), // Convertimos Enum a String para Angular
                entity.getImage()
        );
    }

    public Exercice toEntity(ExerciceDTO dto) {
        Exercice entity = new Exercice();
        entity.setId(dto.id());
        entity.setNom(dto.nom());
        entity.setDescription(dto.description());
        entity.setDuree(dto.duree());
        entity.setCategorie(Categorie.valueOf(dto.categorie()));
        entity.setImage(dto.image());
        return entity;
    }
}
