package com.example.fitnessBackEnd.service;

import com.example.fitnessBackEnd.dto.Fact;
import com.example.fitnessBackEnd.entity.ClassSession;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public final class Facts {
    private Facts() {}
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.FRENCH);

    public static String date(LocalDate d) { return d.format(DATE); }
    public static String time(LocalTime t) { return t.toString().substring(0, 5); }

    public static List<Fact> of(String... kv) {
        List<Fact> list = new ArrayList<>();
        for (int i = 0; i + 1 < kv.length; i += 2) {
            if (kv[i + 1] != null) {
                list.add(new Fact(kv[i], kv[i + 1]));
            }
        }
        return list;
    }

    public static List<Fact> session(ClassSession s) {
        return of("Cours", s.getName(), "Date", date(s.getSessionDate()),
                "Heure", time(s.getStartTime()), "Salle", s.getRoom());
    }

    public static String toText(List<Fact> facts) {
        return facts.stream().map(f -> f.label() + "=" + f.value().replaceAll("[\\r\\n]+", " ").trim())
                .collect(Collectors.joining("\n"));
    }

    public static List<Fact> parse(String text) {
        if (text == null || text.isBlank()) return List.of();
        return text.lines().map(l -> l.split("=", 2)).filter(p -> p.length == 2)
                .map(p -> new Fact(p[0], p[1])).toList();
    }

}
