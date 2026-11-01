package com.example.fitnessBackEnd.service;

import com.example.fitnessBackEnd.dto.DraftItem;
import com.example.fitnessBackEnd.dto.DraftProgram;
import com.example.fitnessBackEnd.entity.Exercice;
import com.example.fitnessBackEnd.repository.ExerciceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProgramValidator {

    private final ExerciceRepository exercices;

    public List<String> validate(DraftProgram d, Set<Integer> requestedDays) {
        if (d == null || d.items() == null || d.items().isEmpty()) return List.of("Le programme est vide");
        List<String> errors = new ArrayList<>();
        if (d.items().size() > 60) errors.add("Trop de lignes (60 maximum)");

        Set<Long> known = exercices.findAllById(
                        d.items().stream().map(DraftItem::exerciceId).filter(Objects::nonNull).toList())
                .stream().map(Exercice::getId).collect(Collectors.toSet());

        Map<Integer, Integer> setsPerDay = new HashMap<>();
        Set<String> seen = new HashSet<>();
        for (DraftItem i : d.items()) {
            if (i.exerciceId() == null || !known.contains(i.exerciceId()))
                errors.add("L'exercice " + i.exerciceId() + " n'existe pas dans le catalogue");
            if (!requestedDays.contains(i.dayOfWeek()))
                errors.add("Le jour " + i.dayOfWeek() + " n'a pas été demandé");
            if (i.sets() < 1 || i.sets() > 8) errors.add("Séries hors limites (1 à 8)");
            if (i.reps() < 1 || i.reps() > 30) errors.add("Répétitions hors limites (1 à 30)");
            if (i.restSeconds() < 15 || i.restSeconds() > 300) errors.add("Repos hors limites (15 à 300 s)");
            if (!seen.add(i.dayOfWeek() + ":" + i.exerciceId()))
                errors.add("Exercice répété le jour " + i.dayOfWeek());
            setsPerDay.merge(i.dayOfWeek(), i.sets(), Integer::sum);
        }
        for (int day : requestedDays)
            if (!setsPerDay.containsKey(day)) errors.add("Aucune séance prévue le jour " + day);
        setsPerDay.forEach((day, total) -> { if (total > 24) errors.add("Plus de 24 séries le jour " + day); });

        return errors.stream().distinct().limit(10).toList();
    }
}
