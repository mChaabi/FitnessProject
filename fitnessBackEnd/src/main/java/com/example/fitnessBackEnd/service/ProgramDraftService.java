package com.example.fitnessBackEnd.service;

import com.example.fitnessBackEnd.dto.DraftProgram;
import com.example.fitnessBackEnd.dto.DraftRequest;
import com.example.fitnessBackEnd.dto.DraftResponse;
import com.example.fitnessBackEnd.entity.AgentRun;
import com.example.fitnessBackEnd.entity.Member;
import com.example.fitnessBackEnd.exception.ConflictException;
import com.example.fitnessBackEnd.exception.NotFoundException;
import com.example.fitnessBackEnd.exception.TooManyRequestsException;
import com.example.fitnessBackEnd.repository.AgentRunRepository;
import com.example.fitnessBackEnd.repository.ExerciceRepository;
import com.example.fitnessBackEnd.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProgramDraftService {

    private static final String GRAPH = "program-draft";

    private final ProgramService programs;
    private final MemberRepository members;
    private final ExerciceRepository exercices;
    private final ProfileBuilder profiles;
    private final ProgramDraftEngine engine;
    private final AgentRunRepository runs;

    @Value("${app.agents.enabled:true}") private boolean enabled;
    @Value("${app.agents.program.max-runs-per-day:10}") private int maxRuns;

    public DraftResponse draft(DraftRequest r, Authentication a) {
        if (!enabled) throw new ConflictException("Cette fonctionnalité est désactivée");
        programs.assertCanManage(r.memberId(), a);                       // 403 si no es su cliente

        long today = runs.countByStartedByAndGraphAndCreatedAtAfter(
                a.getName(), GRAPH, LocalDate.now().atStartOfDay());
        if (today >= maxRuns) throw new TooManyRequestsException(3600);

        Member m = members.findById(r.memberId()).orElseThrow(() -> new NotFoundException("Membre introuvable"));
        String catalog = exercices.findAll().stream().limit(200)
                .map(e -> e.getId() + " : " + e.getNom() + " (" + e.getCategorie() + ")")
                .collect(Collectors.joining("\n"));

        AgentRun run = new AgentRun();
        run.setId(UUID.randomUUID().toString()); run.setGraph(GRAPH); run.setStartedBy(a.getName());
        run.setMemberId(r.memberId()); run.setStatus("RUNNING"); run.setCreatedAt(LocalDateTime.now());
        runs.save(run);

        try {
            ProgramDraftEngine.State s = engine.run(profiles.build(m, r), catalog, r.days());
            boolean ready = s.errors().isEmpty() && s.draft() != null;
            run.setStatus(ready ? "READY" : "FAILED");
            run.setAttempts(s.attempts());
            run.setError(ready ? null : String.join(" ; ", s.errors()).substring(0, Math.min(480, String.join(" ; ", s.errors()).length())));
            run.setFinishedAt(LocalDateTime.now());
            runs.save(run);
            DraftProgram d = s.draft();
            return new DraftResponse(run.getId(), ready, s.attempts(), s.errors(), r.memberId(),
                    d == null ? null : d.name(), d == null ? null : d.goal(), LocalDate.now(),
                    ready ? d.items() : List.of());
        } catch (Exception e) {
            log.error("Fallo del agente de programas [run={}]", run.getId(), e);
            run.setStatus("FAILED"); run.setError("Erreur interne"); run.setFinishedAt(LocalDateTime.now());
            runs.save(run);
            throw new ConflictException("La génération a échoué, créez le programme manuellement");
        }
    }
}
