package com.example.fitnessBackEnd.service;

import com.example.fitnessBackEnd.dto.AssistantReply;
import com.example.fitnessBackEnd.dto.PublicSession;
import com.example.fitnessBackEnd.entity.AgentRun;
import com.example.fitnessBackEnd.entity.Member;
import com.example.fitnessBackEnd.exception.ConflictException;
import com.example.fitnessBackEnd.exception.NotFoundException;
import com.example.fitnessBackEnd.exception.TooManyRequestsException;
import com.example.fitnessBackEnd.repository.AgentRunRepository;
import com.example.fitnessBackEnd.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClassAssistantService {

    private static final String GRAPH = "class-assistant";
    private static final int MAX_SUGGESTIONS = 3;
    private static final int MAX_CATALOG = 80;
    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("EEEE yyyy-MM-dd", Locale.FRENCH);

    private final ObjectProvider<ClassAssistant> assistant;      // empty when the AI is disabled
    private final ClassScheduleService schedule;
    private final MemberRepository members;
    private final AgentRunRepository runs;

    @Value("${app.agents.enabled:true}") private boolean enabled;
    @Value("${app.agents.assistant.max-runs-per-day:20}") private int maxRuns;

    // No @Transactional on purpose: the AI call can take seconds and must not hold a DB connection.
    public AssistantReply suggest(String message, Authentication a) {
        if (!enabled) throw new ConflictException("Cette fonctionnalité est désactivée");
        Member m = members.findByUserEmail(a.getName())
                .orElseThrow(() -> new NotFoundException("Aucun membre lié à votre compte"));

        LocalDate today = LocalDate.now();
        List<PublicSession> catalog = schedule.publicList(today, today.plusDays(13)).stream().limit(MAX_CATALOG).toList();
        if (catalog.isEmpty())
            return new AssistantReply("Aucune séance n'est programmée ces prochains jours.", List.of(), false);

        if (runs.countByStartedByAndGraphAndCreatedAtAfter(a.getName(), GRAPH, today.atStartOfDay()) >= maxRuns)
            throw new TooManyRequestsException(3600);

        AgentRun run = new AgentRun();
        run.setId(UUID.randomUUID().toString()); run.setGraph(GRAPH); run.setStartedBy(a.getName());
        run.setMemberId(m.getId()); run.setStatus("RUNNING"); run.setCreatedAt(LocalDateTime.now());
        runs.save(run);

        String request = clean(message);
        AssistantReply out;
        try {
            out = ask(request, catalog);
        } catch (Exception e) {
            log.warn("Assistant IA indisponible, recherche simple utilisée : {}", e.getMessage());
            out = fallback(request, catalog);
        }
        run.setStatus(out.ai() ? "READY" : "FALLBACK"); run.setAttempts(1); run.setFinishedAt(LocalDateTime.now());
        runs.save(run);
        return out;
    }

    private AssistantReply ask(String request, List<PublicSession> catalog) {
        ClassAssistant ai = assistant.getIfAvailable();
        if (ai == null) return fallback(request, catalog);

        ClassAssistant.ClassPick pick = ai.pick(DAY.format(LocalDate.now()), request, format(catalog));

        Set<Long> valid = catalog.stream().map(PublicSession::id).collect(Collectors.toSet());
        List<Long> ids = pick == null || pick.sessionIds() == null ? List.of()
                : pick.sessionIds().stream().filter(Objects::nonNull).filter(valid::contains)   // invented ids are dropped
                .distinct().limit(MAX_SUGGESTIONS).toList();

        String reply = pick == null ? null : AiTextGuard.clean(pick.reply());   // no digits, links or HTML
        if (reply == null)
            reply = ids.isEmpty() ? "Je n'ai rien trouvé qui corresponde exactement à votre demande."
                    : "Voici ce que je vous propose.";
        return new AssistantReply(reply, ids, true);
    }

    /** Simple keyword search: class name + morning / afternoon / evening. */
    private AssistantReply fallback(String request, List<PublicSession> catalog) {
        String q = request.toLowerCase(Locale.FRENCH);
        boolean named = catalog.stream().anyMatch(s -> nameHit(q, s));
        boolean morning = q.contains("matin"), noon = q.contains("midi"), evening = q.contains("soir");
        boolean timeAsked = morning || noon || evening;

        List<Long> ids = catalog.stream()
                .filter(s -> !named || nameHit(q, s))
                .filter(s -> {
                    int h = s.startTime().getHour();
                    return !timeAsked || (morning && h < 12) || (noon && h >= 12 && h < 17) || (evening && h >= 17);
                })
                .filter(s -> s.freeSpots() > 0)
                .limit(MAX_SUGGESTIONS).map(PublicSession::id).toList();

        return new AssistantReply(ids.isEmpty()
                ? "Je n'ai rien trouvé pour cette demande. Parcourez les cours ci-dessous."
                : "Voici des séances qui correspondent à votre recherche.", ids, false);
    }

    private static boolean nameHit(String q, PublicSession s) {
        return Arrays.stream(s.name().toLowerCase(Locale.FRENCH).split("\\s+"))
                .anyMatch(w -> w.length() >= 3 && q.contains(w));
    }

    private static String format(List<PublicSession> c) {
        return c.stream().map(s -> s.id() + " | " + s.name() + " | " + s.coachName() + " | "
                        + DAY.format(s.date()) + " | " + Facts.time(s.startTime()) + " | " + s.freeSpots() + " places")
                .collect(Collectors.joining("\n"));
    }

    /** Free text typed by a user: remove characters that could break the prompt structure. */
    private static String clean(String s) {
        return s.replaceAll("[\\r\\n{}<>\"`]", " ").replaceAll("\\s+", " ").trim();
    }
}