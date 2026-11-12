package com.example.fitnessBackEnd.mapper;

import com.example.fitnessBackEnd.dto.ExerciceDTO;
import com.example.fitnessBackEnd.entity.Exercice;
import com.example.fitnessBackEnd.enums.Categorie;
import org.springframework.stereotype.Component;

@Component
public class ExerciceMapper {

    public ExerciceDTO toDTO(Exercice e) {
        return new ExerciceDTO(e.getId(), e.getNom(), e.getDescription(), e.getDuree(),
                e.getCategorie().name(), e.getImage(),
                e.getObjectif(), e.getInstructions(), e.getMuscles(), e.getClassKeywords());
    }

    public Exercice toEntity(ExerciceDTO dto) {
        Exercice e = new Exercice();
        e.setId(dto.id());
        e.setNom(dto.nom());
        e.setDescription(dto.description());
        e.setDuree(dto.duree());
        e.setCategorie(Categorie.valueOf(dto.categorie()));
        e.setImage(dto.image());
        e.setObjectif(dto.objectif());
        e.setInstructions(dto.instructions());
        e.setMuscles(dto.muscles());
        e.setClassKeywords(dto.classKeywords());
        return e;
    }
}