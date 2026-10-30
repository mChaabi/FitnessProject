package com.example.fitnessBackEnd.service;

import com.example.fitnessBackEnd.dto.Fact;
import com.example.fitnessBackEnd.entity.Booking;
import com.example.fitnessBackEnd.entity.ClassSession;
import com.example.fitnessBackEnd.entity.Member;
import com.example.fitnessBackEnd.entity.Subscription;
import com.example.fitnessBackEnd.enums.*;
import com.example.fitnessBackEnd.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationJobs {

    private final SessionRepository sessions;
    private final BookingRepository bookings;
    private final SubscriptionRepository subs;
    private final MemberRepository members;
    private final CheckInRepository checkIns;
    private final EmailQueue queue;

    @Value("${app.dashboard.expiring-days:7}")   private int expiringDays;
    @Value("${app.dashboard.inactivity-days:14}") private int inactivityDays;

    @Scheduled(cron = "${app.mail.reminder-cron:0 0 * * * *}")
    public void classReminders() {
        LocalDateTime now = LocalDateTime.now(), from = now.plusHours(23), to = now.plusHours(24);
        for (ClassSession s : sessions.findInRange(from.toLocalDate(), to.toLocalDate())) {
            LocalDateTime start = LocalDateTime.of(s.getSessionDate(), s.getStartTime());
            if (s.getStatus() != SessionStatus.SCHEDULED || start.isBefore(from) || !start.isBefore(to)) continue;
            for (Booking b : bookings.attendees(s.getId(), BookingStatus.CANCELLED)) {
                if (b.getStatus() != BookingStatus.CONFIRMED) continue;
                safe(() -> queue.enqueue(EmailType.CLASS_REMINDER, b.getMember(), Facts.session(s), "REMIND:" + b.getId()));
            }
        }
    }

    @Scheduled(cron = "${app.mail.expiring-cron:0 0 9 * * *}")
    public void expiringSubscriptions() {
        LocalDate today = LocalDate.now();
        for (Subscription s : subs.findExpiring(SubscriptionStatus.ACTIVE, today, today.plusDays(expiringDays))) {
            safe(() -> queue.enqueue(EmailType.SUBSCRIPTION_EXPIRING, s.getMember(),
                    Facts.of("Abonnement", s.getPlan().getName(), "Date de fin", Facts.date(s.getEndDate())),
                    "EXPIRING:" + s.getId()));
        }
    }

    @Scheduled(cron = "${app.mail.risk-cron:0 0 10 * * *}")
    public void churnRisk() {
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();
        List<Member> risk = members.findAtRisk(MemberStatus.ACTIVE, today.minusDays(inactivityDays),
                now.minusDays(inactivityDays), CheckInOutcome.ALLOWED, PageRequest.of(0, 100));
        if (risk.isEmpty()) return;

        Map<Long, LocalDateTime> last = new HashMap<>();
        checkIns.lastVisits(risk.stream().map(Member::getId).toList(), CheckInOutcome.ALLOWED)
                .forEach(r -> last.put((Long) r[0], (LocalDateTime) r[1]));

        for (Member m : risk) {
            LocalDateTime lv = last.get(m.getId());
            String episode = lv == null ? "NEVER" : lv.toLocalDate().toString();
            List<Fact> facts = lv == null
                    ? Facts.of("Situation", "Aucune visite pour l'instant")
                    : Facts.of("Dernière visite", "il y a " + ChronoUnit.DAYS.between(lv.toLocalDate(), today) + " jours");
            safe(() -> queue.enqueue(EmailType.CHURN_RISK, m, facts, "RISK:" + m.getId() + ":" + episode));
        }
    }

    private void safe(Runnable r) {
        try { r.run(); } catch (RuntimeException e) { log.warn("Notificación omitida: {}", e.getMessage()); }
    }
}
