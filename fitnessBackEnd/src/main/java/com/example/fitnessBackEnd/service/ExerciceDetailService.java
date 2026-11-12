package com.example.fitnessBackEnd.service;

import com.example.fitnessBackEnd.dto.ExerciceDetail;
import com.example.fitnessBackEnd.dto.PublicSession;
import com.example.fitnessBackEnd.entity.Exercice;
import com.example.fitnessBackEnd.exception.NotFoundException;
import com.example.fitnessBackEnd.repository.ExerciceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ExerciceDetailService {

    private static final int MAX_SESSIONS = 12;

    private final ExerciceRepository repo;
    private final ClassScheduleService schedule;

    public ExerciceDetail get(Long id) {
        Exercice e = repo.findById(id).orElseThrow(() -> new NotFoundException("Exercice introuvable"));
        List<String> keys = keywords(e);

        LocalDate today = LocalDate.now();
        List<PublicSession> sessions = keys.isEmpty() ? List.of()
                : schedule.publicList(today, today.plusDays(13)).stream()      // 14 days = the public maximum
                .filter(s -> matches(keys, s)).limit(MAX_SESSIONS).toList();

        return new ExerciceDetail(e.getId(), e.getNom(), e.getDescription(), e.getDuree(),
                String.valueOf(e.getCategorie()), e.getImage(),
                e.getObjectif(), e.getInstructions(), e.getMuscles(), sessions);
    }

    /** The configured keywords, plus the exercise name itself. */
    private static List<String> keywords(Exercice e) {
        Set<String> k = new LinkedHashSet<>();
        if (e.getClassKeywords() != null)
            for (String s : e.getClassKeywords().split(",")) add(k, s);
        add(k, e.getNom());
        return List.copyOf(k);
    }

    private static void add(Set<String> k, String s) {
        String n = norm(s);
        if (n.length() >= 3) k.add(n);              // ignore 1-2 letter keywords: they match everything
    }

    private static boolean matches(List<String> keys, PublicSession s) {
        String text = norm(s.name() + " " + (s.description() == null ? "" : s.description()));
        return keys.stream().anyMatch(text::contains);
    }

    /** Lowercase and without accents, so "Étirements" matches "etirements". */
    private static String norm(String s) {
        if (s == null) return "";
        return Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT).trim();
    }
}