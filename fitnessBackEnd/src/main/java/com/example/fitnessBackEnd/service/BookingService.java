package com.example.fitnessBackEnd.service;

import com.example.fitnessBackEnd.dto.AttendeeRow;
import com.example.fitnessBackEnd.dto.BookingResponse;
import com.example.fitnessBackEnd.entity.Booking;
import com.example.fitnessBackEnd.entity.ClassSession;
import com.example.fitnessBackEnd.entity.Member;
import com.example.fitnessBackEnd.entity.User;
import com.example.fitnessBackEnd.enums.*;
import com.example.fitnessBackEnd.exception.BadRequestException;
import com.example.fitnessBackEnd.exception.ConflictException;
import com.example.fitnessBackEnd.exception.NotFoundException;
import com.example.fitnessBackEnd.repository.BookingRepository;
import com.example.fitnessBackEnd.repository.MemberRepository;
import com.example.fitnessBackEnd.repository.SessionRepository;
import com.example.fitnessBackEnd.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final SessionRepository sessions;
    private final BookingRepository bookings;
    private final MemberRepository members;
    private final SubscriptionRepository subs;
    private final EmailQueue emailQueue;

    @Value("${app.classes.cancel-hours:2}") private int cancelHours;

    @Transactional
    public BookingResponse book(Long sessionId, Long memberIdParam, Authentication a) {
        Member m = targetMember(memberIdParam, a);
        ClassSession s = sessions.lockById(sessionId).orElseThrow(() -> new NotFoundException("Séance introuvable"));

        if (s.getStatus() != SessionStatus.SCHEDULED) throw new ConflictException("Cette séance est annulée");
        if (!startsInFuture(s)) throw new ConflictException("Cette séance a déjà commencé");
        if (m.getStatus() == MemberStatus.INACTIVE || m.getStatus() == MemberStatus.FROZEN)
            throw new ConflictException("Compte membre inactif ou gelé");
        if (subs.findCovering(m.getId(), SubscriptionStatus.ACTIVE, s.getSessionDate()).isEmpty())
            throw new ConflictException("Abonnement non valide à cette date");
        if (bookings.countOverlap(m.getId(), s.getSessionDate(), s.getStartTime(), s.getEndTime(),
                s.getId(), BookingStatus.CANCELLED) > 0)
            throw new ConflictException("Déjà inscrit à une autre séance au même moment");

        Booking b = bookings.findBySessionIdAndMemberId(s.getId(), m.getId()).orElseGet(() -> {
            Booking n = new Booking(); n.setSession(s); n.setMember(m); return n; });
        if (b.getStatus() != null && b.getStatus() != BookingStatus.CANCELLED)
            throw new ConflictException("Déjà inscrit à cette séance");

        long confirmed = bookings.countBySessionIdAndStatus(s.getId(), BookingStatus.CONFIRMED);
        b.setStatus(confirmed < s.getCapacity() ? BookingStatus.CONFIRMED : BookingStatus.WAITLIST);
        b.setQueuedAt(LocalDateTime.now());
        bookings.save(b);

        Long pos = b.getStatus() == BookingStatus.WAITLIST
                ? bookings.countBySessionIdAndStatusAndQueuedAtBefore(s.getId(), BookingStatus.WAITLIST, b.getQueuedAt()) + 1
                : null;
        return new BookingResponse(b.getId(), b.getStatus(), pos);
    }

    @Transactional
    public void cancel(Long bookingId, Authentication a) {
        Booking b = bookings.findDetailed(bookingId).orElseThrow(() -> new NotFoundException("Réservation introuvable"));
        boolean staff = isStaff(a);
        if (!staff) {
            User u = b.getMember().getUser();
            if (u == null || !u.getEmail().equalsIgnoreCase(a.getName())) throw new AccessDeniedException("Forbidden");
        }
        ClassSession s = sessions.lockById(b.getSession().getId()).orElseThrow();
        if (b.getStatus() == BookingStatus.CANCELLED) return;

        boolean wasConfirmed = b.getStatus() == BookingStatus.CONFIRMED;
        if (!staff && wasConfirmed && LocalDateTime.now().isAfter(start(s).minusHours(cancelHours)))
            throw new ConflictException("Annulation impossible moins de " + cancelHours + " h avant le début");

        b.setStatus(BookingStatus.CANCELLED);
        if (wasConfirmed) promote(s);
    }

    /** Anula la sesión entera y todas sus reservas. */
    @Transactional
    public void cancelSession(Long sessionId) {
        ClassSession s = sessions.lockById(sessionId).orElseThrow(() -> new NotFoundException("Séance introuvable"));
        if (s.getStatus() == SessionStatus.CANCELLED) return;
        s.setStatus(SessionStatus.CANCELLED);
        for (Booking b : bookings.attendees(sessionId, BookingStatus.CANCELLED)) {      // confirmados y en espera
            emailQueue.enqueue(EmailType.CLASS_CANCELLED, b.getMember(), Facts.session(s), null);
        }
        bookings.cancelAll(sessionId, BookingStatus.CANCELLED);
    }

    @Transactional(readOnly = true)
    public List<AttendeeRow> attendees(Long sessionId, Authentication a) {
        ClassSession s = sessions.findDetailed(sessionId).orElseThrow(() -> new NotFoundException("Séance introuvable"));
        boolean ownCoach = s.getCoach().getUser().getEmail().equalsIgnoreCase(a.getName());
        if (!isStaff(a) && !ownCoach) throw new AccessDeniedException("Forbidden");
        return bookings.attendees(sessionId, BookingStatus.CANCELLED).stream()
                .map(b -> new AttendeeRow(b.getId(), b.getMember().getId(),
                        b.getMember().getFirstName() + " " + b.getMember().getLastName(),
                        b.getStatus(), b.getQueuedAt()))
                .toList();
    }

    // ---------- reglas ----------
    /** Cuando queda una plaza libre, sube al primero de la cola que siga cumpliendo las condiciones. */
    private void promote(ClassSession s) {
        if (s.getStatus() != SessionStatus.SCHEDULED || !startsInFuture(s)) return;
        long confirmed = bookings.countBySessionIdAndStatus(s.getId(), BookingStatus.CONFIRMED);   // auto-flush previo
        for (Booking w : bookings.findBySessionIdAndStatusOrderByQueuedAtAsc(s.getId(), BookingStatus.WAITLIST)) {
            if (confirmed >= s.getCapacity()) break;
            Member m = w.getMember();
            boolean ok = m.getStatus() != MemberStatus.INACTIVE && m.getStatus() != MemberStatus.FROZEN
                    && !subs.findCovering(m.getId(), SubscriptionStatus.ACTIVE, s.getSessionDate()).isEmpty();
            if (ok) {
                w.setStatus(BookingStatus.CONFIRMED);
                confirmed++;
                emailQueue.enqueue(EmailType.WAITLIST_PROMOTED, m, Facts.session(s), null);   // NUEVO
            }
        }
    }

    private Member targetMember(Long memberId, Authentication a) {
        if (hasRole(a, "ROLE_MEMBER")) {
            return members.findByUserEmail(a.getName())
                    .orElseThrow(() -> new NotFoundException("Aucun membre lié à votre compte"));
        }
        if (memberId == null) throw new BadRequestException("Indiquez le membre");
        return members.findById(memberId).orElseThrow(() -> new NotFoundException("Membre introuvable"));
    }

    private boolean isStaff(Authentication a) { return hasRole(a, "ROLE_ADMIN") || hasRole(a, "ROLE_RECEPTION"); }
    private boolean hasRole(Authentication a, String r) {
        return a.getAuthorities().stream().anyMatch(x -> x.getAuthority().equals(r));
    }
    private LocalDateTime start(ClassSession s) { return LocalDateTime.of(s.getSessionDate(), s.getStartTime()); }
    private boolean startsInFuture(ClassSession s) { return start(s).isAfter(LocalDateTime.now()); }
}
