package com.example.fitnessBackEnd.service;

import com.example.fitnessBackEnd.dto.Metric;
import com.example.fitnessBackEnd.enums.EmailStatus;
import com.example.fitnessBackEnd.enums.MemberStatus;
import com.example.fitnessBackEnd.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/** Anonymous facts about the whole application. Coach names are already public on the website. */
@Component
@RequiredArgsConstructor
public class AppKnowledge {

    private static final String[] DAYS = {"", "lundi", "mardi", "mercredi", "jeudi", "vendredi", "samedi", "dimanche"};

    private final MemberRepository members;
    private final CoachRepository coaches;
    private final PlanRepository plans;
    private final TemplateRepository templates;
    private final ExerciceRepository exercices;
    private final VideoRepository videos;
    private final OutboxRepository outbox;
    private final EnrollmentRepository enrollments;
    private final ClassScheduleService schedule;

    public List<Metric> build() {
        List<Metric> m = new ArrayList<>();

        // members (counts only)
        add(m, "members.total", "Membres enregistrés (tous statuts)", members.count());
        for (MemberStatus s : MemberStatus.values())
            add(m, "members.status." + s.name().toLowerCase(), "Membres au statut " + s.name(), members.countByStatus(s));
        add(m, "enrollments.pending", "Inscriptions en attente de règlement", enrollments.findByStatusDetailed("PENDING").size());

        // coaches
        var all = coaches.findAllWithUser();
        var active = all.stream().filter(c -> c.isActive()).toList();
        add(m, "coaches.active", "Coachs actifs", active.size());
        add(m, "coaches.inactive", "Coachs désactivés", all.size() - active.size());
        add(m, "coaches.list", "Coachs actifs", active.stream()
                .map(c -> c.getUser().getPrenom() + " " + c.getUser().getNom()
                        + (c.getSpecialties() == null ? "" : " (" + c.getSpecialties() + ")"))
                .collect(Collectors.joining(" ; ")));

        // subscriptions offered (the plans, not who bought them)
        var ps = plans.findByActiveTrueOrderByPriceAsc();
        add(m, "plans.active_count", "Abonnements proposés (actifs)", ps.size());
        add(m, "plans.list", "Abonnements et tarifs", ps.stream()
                .map(p -> p.getName() + " : " + p.getPrice().stripTrailingZeros().toPlainString()
                        + " MAD pour " + p.getDurationDays() + " jours")
                .collect(Collectors.joining(" ; ")));

        // planning
        var ts = templates.findActiveWithCoach();
        add(m, "classes.templates_active", "Cours récurrents actifs", ts.size());
        add(m, "classes.weekly_list", "Planning hebdomadaire type", ts.stream().limit(20)
                .map(t -> DAYS[t.getDayOfWeek()] + " " + Facts.time(t.getStartTime()) + " " + t.getName()
                        + " (" + t.getCoach().getUser().getPrenom() + ", " + t.getCapacity() + " places)")
                .collect(Collectors.joining(" ; ")));
        LocalDate today = LocalDate.now();
        var up = schedule.publicList(today, today.plusDays(6));
        add(m, "classes.upcoming_count", "Séances à venir (7 jours)", up.size());
        add(m, "classes.upcoming_list", "Prochaines séances", up.stream().limit(15)
                .map(s -> DAYS[s.date().getDayOfWeek().getValue()] + " " + s.date() + " " + Facts.time(s.startTime())
                        + " " + s.name() + " (" + s.freeSpots() + " places libres)")
                .collect(Collectors.joining(" ; ")));

        // catalog
        var ex = exercices.findAll();
        add(m, "exercises.total", "Exercices au catalogue", ex.size());
        add(m, "exercises.by_category", "Exercices par catégorie", counts(ex.stream()
                .collect(Collectors.groupingBy(e -> String.valueOf(e.getCategorie()), TreeMap::new, Collectors.counting()))));
        var vs = videos.findByActiveTrueOrderByCreatedAtDesc();
        add(m, "videos.total", "Vidéos publiées", vs.size());
        add(m, "videos.by_category", "Vidéos par catégorie", counts(vs.stream()
                .collect(Collectors.groupingBy(v -> String.valueOf(v.getCategory()), TreeMap::new, Collectors.counting()))));

        // emails
        add(m, "emails.pending", "Emails en attente d'envoi", outbox.countByStatus(EmailStatus.PENDING));
        add(m, "emails.failed", "Emails en échec", outbox.countByStatus(EmailStatus.FAILED));
        return m;
    }

    private static String counts(Map<String, Long> map) {
        return map.isEmpty() ? "aucun" : map.entrySet().stream().map(e -> e.getKey() + " : " + e.getValue())
                .collect(Collectors.joining(" ; "));
    }

    private static void add(List<Metric> list, String key, String label, Object value) {
        list.add(new Metric(key, label.replaceAll("[|\\r\\n]", " "), String.valueOf(value).replaceAll("[|\\r\\n]", " ")));
    }
}