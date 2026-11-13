package com.example.fitnessBackEnd.service;

import com.example.fitnessBackEnd.dto.DashboardResponse;
import com.example.fitnessBackEnd.dto.Metric;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AnalyticsSnapshot {

    public record Snapshot(DashboardResponse data, List<Metric> metrics) {
        public Map<String, Metric> byKey() {
            return metrics.stream().collect(Collectors.toMap(Metric::key, m -> m, (a, b) -> a, LinkedHashMap::new));
        }
        /** One line per metric: key | label | value. */
        public String asText() {
            return metrics.stream().map(m -> m.key() + " | " + m.label() + " | " + m.value())
                    .collect(Collectors.joining("\n"));
        }
    }

    private final DashboardService dashboard;
    private final AppKnowledge knowledge;

    public Snapshot build() {
        DashboardResponse d = dashboard.build();
        var k = d.kpis();
        List<Metric> m = new ArrayList<>();

        add(m, "members.active", "Membres actifs", k.activeMembers());
        add(m, "members.new_month", "Nouveaux membres ce mois", k.newThisMonth());
        add(m, "data.small_sample", "Échantillon réduit (moins de 10 membres actifs)", k.activeMembers() < 10 ? "oui" : "non");

        add(m, "revenue.month", "Revenus du mois (MAD)", k.revenueThisMonth().toPlainString());
        add(m, "revenue.same_period_last", "Revenus sur la même période du mois dernier (MAD)", k.revenueSamePeriodLastMonth().toPlainString());
        add(m, "revenue.growth_pct", "Évolution des revenus vs même période (%)", k.revenueGrowth() == null ? "non calculable" : k.revenueGrowth());
        d.revenue().forEach(p -> add(m, "revenue." + p.month(), "Revenus " + p.month() + " (MAD)", p.amount().toPlainString()));

        long total = d.visits().stream().mapToLong(DashboardResponse.DayPoint::visits).sum();
        add(m, "visits.total_14d", "Entrées sur 14 jours", total);
        add(m, "visits.avg_per_day", "Entrées moyennes par jour", String.format(Locale.ROOT, "%.1f", total / (double) Math.max(1, d.visits().size())));
        add(m, "visits.zero_days", "Jours sans aucune entrée (sur 14)", d.visits().stream().filter(v -> v.visits() == 0).count());
        d.visits().stream().max(Comparator.comparingLong(DashboardResponse.DayPoint::visits))
                .ifPresent(v -> add(m, "visits.best_day", "Jour le plus fréquenté", v.day() + " (" + v.visits() + " entrées)"));
        add(m, "checkins.today", "Entrées autorisées aujourd'hui", k.entriesToday());
        add(m, "checkins.denied_today", "Entrées refusées aujourd'hui", k.deniedToday());

        int i = 1;
        for (var p : d.plans()) add(m, "plan." + i++, "Abonnés actifs : " + p.plan(), p.count());   // index keys: names may contain odd characters

        add(m, "expiring.count", "Abonnements qui expirent sous " + k.expiringDays() + " jours", k.expiringCount());
        add(m, "expiring.ratio_pct", "Part des membres actifs concernés (%)", pct(k.expiringCount(), k.activeMembers()));
        add(m, "atrisk.count", "Membres actifs absents depuis " + k.inactivityDays() + " jours", k.atRiskCount());
        add(m, "atrisk.ratio_pct", "Part des membres actifs concernés (%)", pct(k.atRiskCount(), k.activeMembers()));

        add(m, "classes.sessions_7d", "Séances prévues sur 7 jours", d.classes().sessions());
        add(m, "classes.avg_occupancy_pct", "Taux d'occupation moyen des cours (%)", d.classes().avgOccupancy());
        add(m, "classes.waiting_total", "Personnes en liste d'attente", d.classes().waitingTotal());

        m.addAll(knowledge.build());

        return new Snapshot(d, m);
    }

    private static String pct(long part, long whole) {
        return whole == 0 ? "non calculable" : String.format(Locale.ROOT, "%.0f", part * 100.0 / whole);
    }

    private static void add(List<Metric> list, String key, String label, Object value) {
        list.add(new Metric(key, label.replaceAll("[|\\r\\n]", " "), String.valueOf(value).replaceAll("[|\\r\\n]", " ")));
    }
}