package com.example.fitnessBackEnd.service;

import com.example.fitnessBackEnd.dto.ExerciceDTO;
import com.example.fitnessBackEnd.entity.Exercice;
import com.example.fitnessBackEnd.mapper.ExerciceMapper;
import com.example.fitnessBackEnd.repository.ExerciceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExerciceService {

    @Autowired
    private ExerciceRepository repository;

    @Autowired
    private ExerciceMapper mapper;

    public List<ExerciceDTO> findAll() {
        return repository.findAll()
                .stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    public ExerciceDTO save(ExerciceDTO dto) {
        Exercice entity = mapper.toEntity(dto);
        Exercice savedEntity = repository.save(entity);
        return mapper.toDTO(savedEntity);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Exercice non trouvé avec l'id : " + id);
        }
        repository.deleteById(id);
    }

    // Aquí podrías añadir lógica como: "calcular calorías quemadas según la duración"
}