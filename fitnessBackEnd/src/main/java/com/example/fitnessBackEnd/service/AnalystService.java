package com.example.fitnessBackEnd.service;

import com.example.fitnessBackEnd.dto.AnalysisReply;
import com.example.fitnessBackEnd.dto.AnalysisReply.InsightView;
import com.example.fitnessBackEnd.dto.Metric;
import com.example.fitnessBackEnd.dto.VideoResponse;
import com.example.fitnessBackEnd.entity.AgentRun;
import com.example.fitnessBackEnd.enums.VideoCategory;
import com.example.fitnessBackEnd.exception.ConflictException;
import com.example.fitnessBackEnd.service.AnalyticsSnapshot.Snapshot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalystService {

    private static final String GRAPH = "analyst";
    private static final Set<String> SEVERITIES = Set.of("POSITIVE", "INFO", "WARNING", "CRITICAL");
    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.FRENCH);
    private static final Pattern FACTUAL = Pattern.compile("^(combien|quel|quels|quelle|quelles|liste|qui|quand|donne|montre|affiche)\\b.*");

    private static final Map<String, VideoCategory> VIDEO_TOPICS = Map.of(
            "cardio", VideoCategory.CARDIO_HIIT, "hiit", VideoCategory.CARDIO_HIIT,
            "muscu", VideoCategory.MUSCULATION, "crossfit", VideoCategory.CROSSFIT, "cross fit", VideoCategory.CROSSFIT,
            "yoga", VideoCategory.YOGA, "etir", VideoCategory.STRETCHING, "stretch", VideoCategory.STRETCHING,
            "attack", VideoCategory.BODY_ATTACK);

    /** Keyword fallback (AI off): topic regex -> metric key prefixes to show. */
    private static final Map<String, List<String>> TOPICS = new LinkedHashMap<>();
    static {
        TOPICS.put("coach", List.of("coaches."));
        TOPICS.put("abonnement|plan|tarif|prix", List.of("plans.", "plan."));
        TOPICS.put("membre|inscrit|adherent", List.of("members.", "enrollments."));
        TOPICS.put("cours|planning|seance|horaire", List.of("classes."));
        TOPICS.put("exercice", List.of("exercises."));
        TOPICS.put("video", List.of("videos."));
        TOPICS.put("email|mail", List.of("emails."));
        TOPICS.put("revenu|paiement|chiffre", List.of("revenue."));
        TOPICS.put("entree|frequentation|visite", List.of("visits.", "checkins."));
    }

    private final ObjectProvider<DataAnalyst> analyst;      // empty when the AI is disabled
    private final AnalyticsSnapshot snapshots;
    private final AgentQuota quota;
    private final VideoService videoService;

    @Value("${app.agents.enabled:true}") private boolean enabled;
    @Value("${app.agents.analyst.max-runs-per-day:30}") private int maxRuns;
    @Value("classpath:prompts/app-guide.txt") private Resource guideFile;
    private String guide;

    public AnalysisReply analyze(String question, Authentication a) {
        if (!enabled) throw new ConflictException("Cette fonctionnalité est désactivée");
        AgentRun run = quota.start(a.getName(), GRAPH, 0L, maxRuns);

        Snapshot snap = snapshots.build();
        String q = clean(question);
        AnalysisReply out;
        try {
            out = ask(q, snap);
        } catch (Exception e) {
            log.warn("Analyste IA indisponible, règles utilisées : {}", e.getMessage());
            out = rules(snap, q);
        }
        quota.finish(run, out.ai() ? "READY" : "FALLBACK");
        return out.withVideos(videosFor(q));
    }

    // ---------- AI path ----------
    private AnalysisReply ask(String question, Snapshot snap) {
        DataAnalyst ai = analyst.getIfAvailable();
        if (ai == null) return rules(snap, question);

        DataAnalyst.Analysis r = ai.analyze(DAY.format(LocalDate.now()), question, snap.asText(), guide());
        if (r == null) return rules(snap, question);

        Map<String, Metric> byKey = snap.byKey();
        List<DataAnalyst.Insight> raw = r.insights() == null ? List.of() : r.insights();   // help answers have none
        List<InsightView> insights = raw.stream().filter(Objects::nonNull)
                .map(i -> ground(i, byKey)).filter(Objects::nonNull).limit(5).toList();

        String summary = safe(r.summary(), 700);
        if (summary == null) return rules(snap, question);

        List<String> actions = r.actions() == null ? List.of()
                : r.actions().stream().map(x -> safe(x, 220)).filter(Objects::nonNull).limit(4).toList();
        if (insights.isEmpty() && !actions.isEmpty()) return rules(snap, question);        // ungrounded advice
        return new AnalysisReply(summary, insights, actions, true, LocalDateTime.now(), List.of());
    }

    /** Keeps an insight only if it points to real metrics. The values shown come from OUR data. */
    private InsightView ground(DataAnalyst.Insight i, Map<String, Metric> byKey) {
        String title = safe(i.title(), 90), detail = safe(i.detail(), 420);
        if (title == null || detail == null || i.metricKeys() == null) return null;
        List<Metric> metrics = i.metricKeys().stream().filter(Objects::nonNull).distinct()
                .map(byKey::get).filter(Objects::nonNull).limit(4).toList();
        if (metrics.isEmpty()) return null;                          // invented or missing keys: dropped
        String sev = i.severity() == null ? "INFO" : i.severity().trim().toUpperCase(Locale.ROOT);
        return new InsightView(title, detail, SEVERITIES.contains(sev) ? sev : "INFO", metrics);
    }

    // ---------- videos: chosen by code, never by the AI ----------
    private List<VideoResponse> videosFor(String question) {
        String n = norm(question);
        if (!(n.contains("video") || n.contains("tuto") || n.contains("demonstration"))) return List.of();
        Set<VideoCategory> wanted = EnumSet.noneOf(VideoCategory.class);
        VIDEO_TOPICS.forEach((k, v) -> { if (n.contains(k)) wanted.add(v); });
        return videoService.published().stream()
                .filter(v -> wanted.isEmpty() || wanted.contains(v.category())).limit(3).toList();
    }

    // ---------- fallback: rules, no AI ----------
    private AnalysisReply rules(Snapshot snap, String question) {
        AnalysisReply byTopic = byTopic(snap, question);
        if (byTopic != null) return byTopic;

        var k = snap.data().kpis();
        var c = snap.data().classes();
        Map<String, Metric> by = snap.byKey();
        List<InsightView> out = new ArrayList<>();
        List<String> actions = new ArrayList<>();

        if (k.revenueGrowth() != null && k.revenueGrowth() < 0) {
            out.add(fixed("Revenus en baisse", "Les revenus du mois sont inférieurs à ceux de la même période du mois dernier.",
                    "CRITICAL", by, "revenue.month", "revenue.same_period_last", "revenue.growth_pct"));
            actions.add("Relancez les membres dont l'abonnement est terminé et vérifiez vos offres.");
        } else if (k.revenueGrowth() != null && k.revenueGrowth() > 0) {
            out.add(fixed("Revenus en hausse", "Les revenus du mois dépassent ceux de la même période du mois dernier.",
                    "POSITIVE", by, "revenue.month", "revenue.growth_pct"));
        }
        if (k.expiringCount() > 0) {
            out.add(fixed("Renouvellements à traiter", "Des abonnements arrivent à échéance prochainement : proposez le renouvellement avant la fin.",
                    "WARNING", by, "expiring.count", "expiring.ratio_pct"));
            actions.add("Contactez les membres de la liste « À renouveler » du tableau de bord.");
        }
        if (k.atRiskCount() > 0) {
            out.add(fixed("Membres inactifs", "Certains membres actifs ne sont pas venus depuis longtemps : ils risquent d'abandonner.",
                    "WARNING", by, "atrisk.count", "atrisk.ratio_pct"));
            actions.add("Appelez ou écrivez aux membres de la liste « Risque d'abandon ».");
        }
        if (c.sessions() > 0 && c.avgOccupancy() < 40) {
            out.add(fixed("Cours peu remplis", "Le taux d'occupation moyen des cours est faible.",
                    "INFO", by, "classes.avg_occupancy_pct", "classes.sessions_7d"));
            actions.add("Mettez en avant vos cours sur le site ou changez les horaires peu demandés.");
        } else if (c.sessions() > 0 && c.avgOccupancy() > 85) {
            out.add(fixed("Cours presque complets", "Les cours sont très demandés : pensez à ajouter des séances.",
                    "POSITIVE", by, "classes.avg_occupancy_pct", "classes.waiting_total"));
        }
        if (out.isEmpty()) out.add(fixed("Aucun signal d'alerte", "Aucun point d'attention détecté dans les données actuelles.",
                "INFO", by, "members.active", "visits.total_14d"));

        return new AnalysisReply("Analyse automatique basée sur des règles (l'assistant IA est indisponible).",
                out, actions.stream().limit(4).toList(), false, LocalDateTime.now(), List.of());
    }

    /** "Combien d'abonnements et de coachs ?" without AI: show the metrics of every topic mentioned. */
    private AnalysisReply byTopic(Snapshot snap, String question) {
        String n = norm(question);
        if (!FACTUAL.matcher(n).matches()) return null;
        List<Metric> found = new ArrayList<>();
        TOPICS.forEach((regex, prefixes) -> {
            if (!Pattern.compile(regex).matcher(n).find()) return;
            snap.metrics().stream().filter(m -> prefixes.stream().anyMatch(m.key()::startsWith)).forEach(found::add);
        });
        if (found.isEmpty()) return null;
        return new AnalysisReply("Voici les informations disponibles (réponse automatique : l'assistant IA est indisponible).",
                List.of(new InsightView("Données trouvées", "Valeurs actuelles de votre application.", "INFO",
                        found.stream().distinct().limit(10).toList())),
                List.of(), false, LocalDateTime.now(), List.of());
    }

    private static InsightView fixed(String title, String detail, String sev, Map<String, Metric> by, String... keys) {
        return new InsightView(title, detail, sev, Arrays.stream(keys).map(by::get).filter(Objects::nonNull).toList());
    }

    private synchronized String guide() {
        if (guide == null) {
            try (var in = guideFile.getInputStream()) { guide = new String(in.readAllBytes(), StandardCharsets.UTF_8); }
            catch (IOException e) { log.warn("Guide introuvable : {}", e.getMessage()); guide = ""; }
        }
        return guide;
    }

    // ---------- text safety ----------
    /** Free text typed by the admin: remove characters that could break the prompt structure. */
    private static String clean(String s) {
        String t = s == null ? "" : s.replaceAll("[\\r\\n{}<>\"`]", " ").replaceAll("\\s+", " ").trim();
        return t.length() < 3 ? "Fais une analyse globale de l'activité de la salle." : t;
    }

    /** Text produced by the model: no HTML or links, bounded length. */
    private static String safe(String s, int max) {
        if (s == null) return null;
        String t = s.replaceAll("<[^>]*>", " ").replaceAll("[\\r\\n]+", " ").replaceAll("\\s+", " ").trim();
        if (t.isEmpty() || t.toLowerCase(Locale.ROOT).contains("http") || t.contains("www.")) return null;
        return t.length() > max ? t.substring(0, max - 1) + "…" : t;
    }

    private static String norm(String s) {
        return Normalizer.normalize(s == null ? "" : s, Normalizer.Form.NFD).replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT).trim();
    }
}