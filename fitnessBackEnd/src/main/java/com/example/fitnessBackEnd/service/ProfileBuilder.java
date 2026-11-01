package com.example.fitnessBackEnd.service;

import com.example.fitnessBackEnd.dto.DraftRequest;
import com.example.fitnessBackEnd.entity.Member;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Period;

@Component
public class ProfileBuilder {

    public String build(Member m, DraftRequest r) {
        return "Niveau : " + r.level() + " ; jours disponibles : " + r.days().stream().sorted().toList()
                + " ; durée par séance : " + r.minutesPerSession() + " min"
                + " ; tranche d'âge : " + ageBracket(m.getBirthDate())
                + " ; objectif : " + sanitize(m.getGoal());
    }

    private String ageBracket(LocalDate birth) {
        if (birth == null) return "inconnue";
        int age = Period.between(birth, LocalDate.now()).getYears();
        return age < 18 ? "moins de 18 ans" : age < 30 ? "18-29" : age < 45 ? "30-44" : age < 60 ? "45-59" : "60+";
    }

    /** El objetivo es texto libre escrito por personal: se limita y se limpia. */
    private String sanitize(String s) {
        if (s == null || s.isBlank()) return "non précisé";
        return s.replaceAll("[\\r\\n{}<>\"`]", " ").replaceAll("\\s+", " ").trim()
                .substring(0, Math.min(100, s.trim().length()));
    }
}
