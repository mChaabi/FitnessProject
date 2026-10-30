package com.example.fitnessBackEnd.service;

import com.example.fitnessBackEnd.dto.*;
import com.example.fitnessBackEnd.entity.*;
import com.example.fitnessBackEnd.exception.BadRequestException;
import com.example.fitnessBackEnd.exception.NotFoundException;
import com.example.fitnessBackEnd.repository.CoachRepository;
import com.example.fitnessBackEnd.repository.ExerciceRepository;
import com.example.fitnessBackEnd.repository.MemberRepository;
import com.example.fitnessBackEnd.repository.ProgramRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProgramService {
    private final ProgramRepository repo;
    private final MemberRepository memberRepo;
    private final CoachRepository coachRepo;
    private final ExerciceRepository exerciceRepo;

    @Transactional
    public ProgramResponse create(ProgramRequest r, Authentication auth) {
        Member m = memberRepo.findById(r.memberId()).orElseThrow(() -> new NotFoundException("Membre introuvable"));
        WorkoutProgram p = new WorkoutProgram();
        p.setMember(m);
        p.setCoach(coachFor(m, auth));
        fill(p, r);
        return map(repo.save(p));
    }

    @Transactional
    public ProgramResponse update(Long id, ProgramRequest r, Authentication auth) {
        WorkoutProgram p = detailed(id);
        assertOwner(p, auth);
        fill(p, r);                       // r.memberId() se ignora: un programa no cambia de miembro
        return map(p);
    }

    @Transactional
    public void delete(Long id, Authentication auth) {
        WorkoutProgram p = detailed(id);
        assertOwner(p, auth);
        repo.delete(p);
    }

    @Transactional(readOnly = true)
    public ProgramResponse get(Long id, Authentication auth) {
        WorkoutProgram p = detailed(id);
        boolean mine = p.getMember().getUser() != null
                && p.getMember().getUser().getEmail().equalsIgnoreCase(auth.getName());
        if (!isAdmin(auth) && !ownsAsCoach(p, auth) && !mine) throw new AccessDeniedException("Forbidden");
        return map(p);
    }

    @Transactional(readOnly = true)
    public List<ProgramSummary> forMember(Long memberId, Authentication auth) {
        Member m = memberRepo.findById(memberId).orElseThrow(() -> new NotFoundException("Membre introuvable"));
        if (!isAdmin(auth)) {
            Coach me = coachRepo.findByUserEmail(auth.getName()).orElseThrow(() -> new AccessDeniedException("Forbidden"));
            if (m.getCoach() == null || !m.getCoach().getId().equals(me.getId())) throw new AccessDeniedException("Forbidden");
        }
        return repo.findByMember(memberId).stream().map(p -> new ProgramSummary(p.getId(), p.getName(),
                p.getStartDate(), p.isActive(), name(p.getMember()), name(p.getCoach()))).toList();
    }

    @Transactional(readOnly = true)
    public List<ProgramResponse> mine(String email) {
        return memberRepo.findByUserEmail(email)
                .map(m -> repo.findActiveFull(m.getId()).stream().map(this::map).toList())
                .orElse(List.of());                 // miembro sin cuenta enlazada: lista vacía
    }

    // ---------- reglas ----------
    private Coach coachFor(Member m, Authentication a) {
        if (isAdmin(a)) {
            if (m.getCoach() == null) throw new BadRequestException("Assignez d'abord un coach à ce membre");
            return m.getCoach();
        }
        Coach me = coachRepo.findByUserEmail(a.getName()).filter(Coach::isActive)
                .orElseThrow(() -> new AccessDeniedException("Forbidden"));
        if (m.getCoach() == null || !m.getCoach().getId().equals(me.getId())) throw new AccessDeniedException("Not your client");
        return me;
    }

    private void assertOwner(WorkoutProgram p, Authentication a) {
        if (!isAdmin(a) && !ownsAsCoach(p, a)) throw new AccessDeniedException("Forbidden");
    }
    private boolean ownsAsCoach(WorkoutProgram p, Authentication a) {
        return p.getCoach().getUser().getEmail().equalsIgnoreCase(a.getName());
    }
    private boolean isAdmin(Authentication a) {
        return a.getAuthorities().stream().anyMatch(x -> x.getAuthority().equals("ROLE_ADMIN"));
    }

    private WorkoutProgram detailed(Long id) {
        return repo.findDetailed(id).orElseThrow(() -> new NotFoundException("Programme introuvable"));
    }

    /** Reemplaza las líneas completas: más simple y seguro que fusionar una a una. */
    private void fill(WorkoutProgram p, ProgramRequest r) {
        p.setName(r.name().trim()); p.setGoal(r.goal()); p.setStartDate(r.startDate()); p.setActive(r.active());

        Set<Long> ids = r.items().stream().map(ItemRequest::exerciceId).collect(Collectors.toSet());
        Map<Long, Exercice> byId = exerciceRepo.findAllById(ids).stream()
                .collect(Collectors.toMap(Exercice::getId, Function.identity()));

        p.getItems().clear();
        int pos = 0;
        for (ItemRequest i : r.items()) {
            Exercice e = byId.get(i.exerciceId());
            if (e == null) throw new BadRequestException("Exercice introuvable : " + i.exerciceId());
            WorkoutItem it = new WorkoutItem();
            it.setProgram(p); it.setExercice(e); it.setPosition(pos++);
            it.setDayOfWeek(i.dayOfWeek()); it.setSets(i.sets()); it.setReps(i.reps());
            it.setRestSeconds(i.restSeconds()); it.setNotes(i.notes());
            p.getItems().add(it);
        }
    }

    private ProgramResponse map(WorkoutProgram p) {
        return new ProgramResponse(p.getId(), p.getName(), p.getGoal(), p.getStartDate(), p.isActive(),
                p.getMember().getId(), name(p.getMember()), name(p.getCoach()),
                p.getItems().stream().map(i -> new ItemResponse(i.getId(), i.getExercice().getId(),
                        i.getExercice().getNom(), String.valueOf(i.getExercice().getCategorie()),
                        i.getDayOfWeek(), i.getSets(), i.getReps(), i.getRestSeconds(), i.getNotes())).toList());
    }
    private String name(Member m) { return m.getFirstName() + " " + m.getLastName(); }
    private String name(Coach c) { return c.getUser().getPrenom() + " " + c.getUser().getNom(); }
}
