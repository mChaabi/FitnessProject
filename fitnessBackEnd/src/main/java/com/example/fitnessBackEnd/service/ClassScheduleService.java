package com.example.fitnessBackEnd.service;

import com.example.fitnessBackEnd.dto.SessionRequest;
import com.example.fitnessBackEnd.dto.SessionResponse;
import com.example.fitnessBackEnd.dto.TemplateRequest;
import com.example.fitnessBackEnd.dto.TemplateResponse;
import com.example.fitnessBackEnd.entity.*;
import com.example.fitnessBackEnd.enums.BookingStatus;
import com.example.fitnessBackEnd.enums.SessionStatus;
import com.example.fitnessBackEnd.exception.BadRequestException;
import com.example.fitnessBackEnd.exception.ConflictException;
import com.example.fitnessBackEnd.exception.NotFoundException;
import com.example.fitnessBackEnd.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClassScheduleService {

    private final TemplateRepository templates;
    private final SessionRepository sessions;
    private final BookingRepository bookings;
    private final CoachRepository coaches;
    private final MemberRepository members;

    @Value("${app.classes.generate-days:28}") private int horizon;

    // ---------- plantillas ----------
    @Transactional(readOnly = true)
    public List<TemplateResponse> listTemplates() {
        return templates.findAllWithCoach().stream().map(this::map).toList();
    }

    public TemplateResponse saveTemplate(Long id, TemplateRequest r) {
        ClassTemplate t = (id == null)
                ? new ClassTemplate()
                : templates.findById(id).orElseThrow(() -> new NotFoundException("Modèle introuvable"));

        if (!r.startTime().plusMinutes(r.durationMinutes()).isAfter(r.startTime())) {
            throw new BadRequestException("La séance ne peut pas dépasser minuit");
        }

        t.setName(r.name().trim());
        t.setDescription(r.description());
        t.setCoach(activeCoach(r.coachId()));
        t.setDayOfWeek(r.dayOfWeek());
        t.setStartTime(r.startTime());
        t.setDurationMinutes(r.durationMinutes());
        t.setCapacity(r.capacity());
        t.setRoom(r.room());
        t.setActive(r.active());

        ClassTemplate saved = templates.save(t);

        // Generar sesiones automáticas
        generate(LocalDate.now(), LocalDate.now().plusDays(horizon));

        // Fix: extraemos el ID a una variable final para la expresión lambda
        final Long savedId = saved.getId();

        return map(templates.findAllWithCoach().stream()
                .filter(x -> x.getId().equals(savedId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Modèle introuvable")));
    }

    // ---------- generación ----------
    @Transactional
    public int generate(LocalDate from, LocalDate to) {
        int created = 0;
        for (ClassTemplate t : templates.findActiveWithCoach()) {
            LocalTime end = t.getStartTime().plusMinutes(t.getDurationMinutes());
            for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
                if (d.getDayOfWeek().getValue() != t.getDayOfWeek()) continue;
                if (sessions.existsByTemplateIdAndSessionDate(t.getId(), d)) continue;
                if (sessions.countCoachOverlap(t.getCoach().getId(), d, t.getStartTime(), end, SessionStatus.SCHEDULED) > 0) {
                    log.warn("Coach occupé, séance non générée : {} le {}", t.getName(), d);
                    continue;
                }
                ClassSession s = new ClassSession();
                s.setTemplate(t); s.setName(t.getName()); s.setDescription(t.getDescription());
                s.setCoach(t.getCoach()); s.setSessionDate(d); s.setStartTime(t.getStartTime()); s.setEndTime(end);
                s.setCapacity(t.getCapacity()); s.setRoom(t.getRoom());
                sessions.save(s); created++;
            }
        }
        return created;
    }

    // ---------- sesión suelta ----------
    @Transactional
    public SessionResponse createSession(SessionRequest r) {
        LocalTime end = r.startTime().plusMinutes(r.durationMinutes());
        if (!end.isAfter(r.startTime())) throw new BadRequestException("La séance ne peut pas dépasser minuit");
        if (LocalDateTime.of(r.date(), r.startTime()).isBefore(LocalDateTime.now()))
            throw new BadRequestException("Cette heure est déjà passée");
        Coach c = activeCoach(r.coachId());
        if (sessions.countCoachOverlap(c.getId(), r.date(), r.startTime(), end, SessionStatus.SCHEDULED) > 0)
            throw new ConflictException("Ce coach a déjà une séance à cette heure");

        ClassSession s = new ClassSession();
        s.setName(r.name().trim()); s.setDescription(r.description()); s.setCoach(c);
        s.setSessionDate(r.date()); s.setStartTime(r.startTime()); s.setEndTime(end);
        s.setCapacity(r.capacity()); s.setRoom(r.room());
        sessions.save(s);
        return toResponse(s, 0, 0, null, null);
    }

    // ---------- lectura del planning ----------
    @Transactional(readOnly = true)
    public List<SessionResponse> list(LocalDate from, LocalDate to, Authentication a) {
        if (to.isBefore(from) || ChronoUnit.DAYS.between(from, to) > 62)
            throw new BadRequestException("Période invalide (62 jours maximum)");

        Map<Long, long[]> counts = new HashMap<>();                         // [confirmados, en espera]
        for (Object[] row : bookings.counts(from, to, BookingStatus.CANCELLED)) {
            long[] c = counts.computeIfAbsent((Long) row[0], k -> new long[2]);
            if (row[1] == BookingStatus.CONFIRMED) c[0] = (Long) row[2]; else c[1] = (Long) row[2];
        }

        Map<Long, Booking> mine = new HashMap<>();
        if (a.getAuthorities().stream().anyMatch(x -> x.getAuthority().equals("ROLE_MEMBER"))) {
            members.findByUserEmail(a.getName()).ifPresent(m ->
                    bookings.mineInRange(m.getId(), from, to, BookingStatus.CANCELLED)
                            .forEach(b -> mine.put(b.getSession().getId(), b)));
        }

        return sessions.findInRange(from, to).stream().map(s -> {
            long[] c = counts.getOrDefault(s.getId(), new long[2]);
            return toResponse(s, (int) c[0], (int) c[1], mine.get(s.getId()), a);
        }).toList();
    }

    // ---------- utilidades ----------
    private Coach activeCoach(Long id) {
        Coach c = coaches.findById(id).orElseThrow(() -> new NotFoundException("Coach introuvable"));
        if (!c.isActive()) throw new ConflictException("Ce coach est désactivé");
        return c;
    }

    private SessionResponse toResponse(ClassSession s, int confirmed, int waiting, Booking mine, Authentication a) {
        Long position = null;
        if (mine != null && mine.getStatus() == BookingStatus.WAITLIST) {
            position = bookings.countBySessionIdAndStatusAndQueuedAtBefore(
                    s.getId(), BookingStatus.WAITLIST, mine.getQueuedAt()) + 1;
        }
        boolean future = LocalDateTime.of(s.getSessionDate(), s.getStartTime()).isAfter(LocalDateTime.now());
        return new SessionResponse(s.getId(), s.getTemplate() == null ? null : s.getTemplate().getId(),
                s.getName(), s.getDescription(),
                s.getCoach().getUser().getPrenom() + " " + s.getCoach().getUser().getNom(),
                s.getSessionDate(), s.getStartTime(), s.getEndTime(), s.getCapacity(), s.getRoom(), s.getStatus(),
                confirmed, waiting, mine == null ? null : mine.getStatus(), mine == null ? null : mine.getId(),
                position, s.getStatus() == SessionStatus.SCHEDULED && future);
    }

    private TemplateResponse map(ClassTemplate t) {
        User u = t.getCoach().getUser();
        return new TemplateResponse(t.getId(), t.getName(), t.getDescription(), t.getCoach().getId(),
                u.getPrenom() + " " + u.getNom(), t.getDayOfWeek(), t.getStartTime(),
                t.getDurationMinutes(), t.getCapacity(), t.getRoom(), t.isActive());
    }
}
