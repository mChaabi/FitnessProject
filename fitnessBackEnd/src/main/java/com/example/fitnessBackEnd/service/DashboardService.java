package com.example.fitnessBackEnd.service;

import com.example.fitnessBackEnd.dto.DashboardResponse;
import com.example.fitnessBackEnd.entity.ClassSession;
import com.example.fitnessBackEnd.entity.Member;
import com.example.fitnessBackEnd.entity.Subscription;
import com.example.fitnessBackEnd.enums.*;
import com.example.fitnessBackEnd.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final int LIST_LIMIT = 10;
    private static final int TREND_MONTHS = 6;
    private static final int TREND_DAYS = 14;

    private final MemberRepository members;
    private final SubscriptionRepository subs;
    private final PaymentRepository payments;
    private final CheckInRepository checkIns;
    private final SessionRepository sessions;
    private final BookingRepository bookings;

    @Value("${app.dashboard.inactivity-days:14}") private int inactivityDays;
    @Value("${app.dashboard.expiring-days:7}")   private int expiringDays;

    @Transactional(readOnly = true)
    public DashboardResponse build() {
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        var expiring = expiring(today);
        var risk = atRisk(today, now);

        // ----- ingresos: este mes vs MISMO periodo del mes pasado -----
        LocalDate first = today.withDayOfMonth(1);
        LocalDate lastFirst = first.minusMonths(1);
        LocalDate lastEnd = min(lastFirst.plusDays(today.getDayOfMonth()), first);   // febrero es más corto
        BigDecimal thisMonth = payments.sumBetween(first.atStartOfDay(), first.plusMonths(1).atStartOfDay());
        BigDecimal samePeriod = payments.sumBetween(lastFirst.atStartOfDay(), lastEnd.atStartOfDay());
        Integer growth = samePeriod.signum() == 0 ? null
                : thisMonth.subtract(samePeriod).multiply(BigDecimal.valueOf(100))
                .divide(samePeriod, 0, RoundingMode.HALF_UP).intValue();

        var kpis = new DashboardResponse.Kpis(
                members.countByStatus(MemberStatus.ACTIVE), members.countByJoinedAtGreaterThanEqual(first),
                thisMonth, samePeriod, growth,
                checkIns.countMembers(CheckInOutcome.ALLOWED, today.atStartOfDay(), today.plusDays(1).atStartOfDay()),
                checkIns.countMembers(CheckInOutcome.DENIED, today.atStartOfDay(), today.plusDays(1).atStartOfDay()),
                expiring.total(), risk.total(), expiringDays, inactivityDays);

        return new DashboardResponse(kpis, revenue(first), visits(today), plans(today),
                expiring.rows(), risk.rows(), classes(today), now);
    }

    // ---------- bloques ----------
    private List<DashboardResponse.MonthPoint> revenue(LocalDate firstOfMonth) {
        List<DashboardResponse.MonthPoint> out = new ArrayList<>();
        for (int i = TREND_MONTHS - 1; i >= 0; i--) {
            LocalDate m = firstOfMonth.minusMonths(i);
            out.add(new DashboardResponse.MonthPoint(YearMonth.from(m).toString(),
                    payments.sumBetween(m.atStartOfDay(), m.plusMonths(1).atStartOfDay())));
        }
        return out;
    }

    private List<DashboardResponse.DayPoint> visits(LocalDate today) {
        Map<LocalDate, Long> perDay = new HashMap<>();
        checkIns.visitTimes(today.minusDays(TREND_DAYS - 1L).atStartOfDay(), CheckInOutcome.ALLOWED)
                .forEach(t -> perDay.merge(t.toLocalDate(), 1L, Long::sum));
        return IntStream.range(0, TREND_DAYS).mapToObj(i -> today.minusDays(TREND_DAYS - 1L - i))
                .map(d -> new DashboardResponse.DayPoint(d, perDay.getOrDefault(d, 0L))).toList();   // los días sin datos valen 0
    }

    private List<DashboardResponse.PlanSlice> plans(LocalDate today) {
        return subs.planDistribution(SubscriptionStatus.ACTIVE, today).stream()
                .map(r -> new DashboardResponse.PlanSlice((String) r[0], (Long) r[1])).toList();
    }

    private Block<DashboardResponse.ExpiringRow> expiring(LocalDate today) {
        List<Subscription> all = subs.findExpiring(SubscriptionStatus.ACTIVE, today, today.plusDays(expiringDays));
        var rows = all.stream().limit(LIST_LIMIT).map(s -> new DashboardResponse.ExpiringRow(
                s.getMember().getId(), name(s.getMember()), s.getMember().getPhone(), s.getPlan().getName(),
                s.getEndDate(), ChronoUnit.DAYS.between(today, s.getEndDate()))).toList();
        return new Block<>(rows, all.size());
    }

    private Block<DashboardResponse.AtRiskRow> atRisk(LocalDate today, LocalDateTime now) {
        LocalDate since = today.minusDays(inactivityDays);
        LocalDateTime sinceTime = now.minusDays(inactivityDays);
        long total = members.countAtRisk(MemberStatus.ACTIVE, since, sinceTime, CheckInOutcome.ALLOWED);
        List<Member> list = members.findAtRisk(MemberStatus.ACTIVE, since, sinceTime, CheckInOutcome.ALLOWED,
                PageRequest.of(0, LIST_LIMIT));

        Map<Long, LocalDateTime> last = new HashMap<>();
        if (!list.isEmpty()) {
            checkIns.lastVisits(list.stream().map(Member::getId).toList(), CheckInOutcome.ALLOWED)
                    .forEach(r -> last.put((Long) r[0], (LocalDateTime) r[1]));
        }
        var rows = list.stream().map(m -> {
            LocalDateTime lv = last.get(m.getId());
            return new DashboardResponse.AtRiskRow(m.getId(), name(m), m.getPhone(), lv,
                    lv == null ? null : ChronoUnit.DAYS.between(lv.toLocalDate(), today));   // null = nunca ha venido
        }).toList();
        return new Block<>(rows, total);
    }

    private DashboardResponse.ClassStats classes(LocalDate today) {
        LocalDate to = today.plusDays(6);
        Map<Long, int[]> counts = new HashMap<>();                                   // [confirmados, en espera]
        for (Object[] r : bookings.counts(today, to, BookingStatus.CANCELLED)) {
            int[] c = counts.computeIfAbsent((Long) r[0], k -> new int[2]);
            if (r[1] == BookingStatus.CONFIRMED) c[0] = ((Long) r[2]).intValue(); else c[1] = ((Long) r[2]).intValue();
        }
        LocalDateTime now = LocalDateTime.now();
        var list = sessions.findInRange(today, to).stream()
                .filter(s -> s.getStatus() == SessionStatus.SCHEDULED
                        && LocalDateTime.of(s.getSessionDate(), s.getStartTime()).isAfter(now)).toList();

        int confirmed = 0, capacity = 0; long waiting = 0;
        List<DashboardResponse.SessionLoad> rows = new ArrayList<>();
        for (ClassSession s : list) {
            int[] c = counts.getOrDefault(s.getId(), new int[2]);
            confirmed += c[0]; capacity += s.getCapacity(); waiting += c[1];
            if (rows.size() < 6) rows.add(new DashboardResponse.SessionLoad(s.getId(), s.getName(),
                    s.getSessionDate(), s.getStartTime(), c[0], s.getCapacity(), c[1]));
        }
        int avg = capacity == 0 ? 0 : Math.round(confirmed * 100f / capacity);
        return new DashboardResponse.ClassStats(list.size(), avg, waiting, rows);
    }

    // ---------- utilidades ----------
    private record Block<T>(List<T> rows, long total) {}
    private static LocalDate min(LocalDate a, LocalDate b) { return a.isBefore(b) ? a : b; }
    private static String name(Member m) { return m.getFirstName() + " " + m.getLastName(); }
}