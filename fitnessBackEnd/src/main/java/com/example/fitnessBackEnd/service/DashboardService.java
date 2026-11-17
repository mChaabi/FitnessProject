package com.example.fitnessBackEnd.service;

import com.example.fitnessBackEnd.dto.DashboardResponse;
import com.example.fitnessBackEnd.entity.ClassSession;
import com.example.fitnessBackEnd.entity.Coach;
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
import java.util.*;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final int LIST_LIMIT = 10;
    private static final int TREND_MONTHS = 6;

    private final MemberRepository members;
    private final SubscriptionRepository subs;
    private final PaymentRepository payments;
    private final CheckInRepository checkIns;
    private final SessionRepository sessions;
    private final BookingRepository bookings;
    private final CoachRepository coaches;

    @Value("${app.dashboard.inactivity-days:14}") private int inactivityDays;
    @Value("${app.dashboard.expiring-days:7}")   private int expiringDays;

    /** Used by the AI analyst: 14-day series as before. */
    @Transactional(readOnly = true)
    public DashboardResponse build() { return build(14); }

    @Transactional(readOnly = true)
    public DashboardResponse build(int days) {
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        var expiring = expiring(today);
        var risk = atRisk(today, now);

        // ----- revenue: this month vs the SAME period of last month (unchanged) -----
        LocalDate first = today.withDayOfMonth(1);
        LocalDate lastFirst = first.minusMonths(1);
        LocalDate lastEnd = min(lastFirst.plusDays(today.getDayOfMonth()), first);
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

        // ----- the selected period and the one just before it, same length -----
        LocalDate from = today.minusDays(days - 1L), prevFrom = from.minusDays(days);
        LocalDateTime fromT = from.atStartOfDay(), toT = today.plusDays(1).atStartOfDay(), prevFromT = prevFrom.atStartOfDay();

        // one query for all entries: series, totals and heatmap are computed from it
        long visits = 0, visitsPrev = 0;
        Map<LocalDate, Long> perDay = new HashMap<>();
        int[][] heat = new int[7][24];
        for (LocalDateTime t : checkIns.visitTimes(prevFromT, CheckInOutcome.ALLOWED)) {
            if (t.isBefore(fromT)) { visitsPrev++; continue; }
            visits++;
            heat[t.getDayOfWeek().getValue() - 1][t.getHour()]++;
            perDay.merge(t.toLocalDate(), 1L, Long::sum);
        }
        var series = IntStream.range(0, days).mapToObj(i -> from.plusDays(i))
                .map(d -> new DashboardResponse.DayPoint(d, perDay.getOrDefault(d, 0L))).toList();   // empty days = 0

        List<Coach> activeCoaches = coaches.findAllWithUser().stream().filter(Coach::isActive).toList();
        var period = new DashboardResponse.PeriodStats(days, from,
                members.countByJoinedAtBetween(from, today), members.countByJoinedAtBetween(prevFrom, from.minusDays(1)),
                payments.sumBetween(fromT, toT), payments.sumBetween(prevFromT, fromT),
                visits, visitsPrev,
                checkIns.countMembers(CheckInOutcome.ALLOWED, fromT, toT),
                checkIns.countMembers(CheckInOutcome.ALLOWED, prevFromT, fromT),
                activeCoaches.size());

        var teaching = teaching(from, today, now, activeCoaches);

        return new DashboardResponse(kpis, revenue(first), series, plans(today), expiring.rows(), risk.rows(),
                classes(today), now,
                period, memberStatus(), memberGrowth(first), heat,
                planStats(today, fromT, toT), teaching.coaches(), teaching.classes());
    }

    // ---------- blocks ----------
    private List<DashboardResponse.MonthPoint> revenue(LocalDate firstOfMonth) {
        List<DashboardResponse.MonthPoint> out = new ArrayList<>();
        for (int i = TREND_MONTHS - 1; i >= 0; i--) {
            LocalDate m = firstOfMonth.minusMonths(i);
            out.add(new DashboardResponse.MonthPoint(YearMonth.from(m).toString(),
                    payments.sumBetween(m.atStartOfDay(), m.plusMonths(1).atStartOfDay())));
        }
        return out;
    }

    private List<DashboardResponse.MonthCount> memberGrowth(LocalDate firstOfMonth) {
        List<DashboardResponse.MonthCount> out = new ArrayList<>();
        for (int i = TREND_MONTHS - 1; i >= 0; i--) {
            LocalDate m = firstOfMonth.minusMonths(i);
            out.add(new DashboardResponse.MonthCount(YearMonth.from(m).toString(),
                    members.countByJoinedAtBetween(m, m.plusMonths(1).minusDays(1))));
        }
        return out;
    }

    private List<DashboardResponse.StatusSlice> memberStatus() {
        return Arrays.stream(MemberStatus.values())
                .map(s -> new DashboardResponse.StatusSlice(s.name(), members.countByStatus(s))).toList();
    }

    private List<DashboardResponse.PlanSlice> plans(LocalDate today) {
        return subs.planDistribution(SubscriptionStatus.ACTIVE, today).stream()
                .map(r -> new DashboardResponse.PlanSlice((String) r[0], (Long) r[1])).toList();
    }

    /** Active subscribers per plan now + revenue of the selected period (a plan can have revenue and no active subscriber). */
    private List<DashboardResponse.PlanStat> planStats(LocalDate today, LocalDateTime from, LocalDateTime to) {
        Map<String, Long> active = new LinkedHashMap<>();
        subs.planDistribution(SubscriptionStatus.ACTIVE, today).forEach(r -> active.put((String) r[0], (Long) r[1]));
        Map<String, BigDecimal> revenue = new HashMap<>();
        payments.revenueByPlan(from, to).forEach(r -> revenue.put((String) r[0], new BigDecimal(String.valueOf(r[1]))));

        Set<String> names = new LinkedHashSet<>(active.keySet());
        names.addAll(revenue.keySet());
        return names.stream()
                .map(n -> new DashboardResponse.PlanStat(n, active.getOrDefault(n, 0L), revenue.getOrDefault(n, BigDecimal.ZERO)))
                .sorted(Comparator.comparing(DashboardResponse.PlanStat::revenue).reversed()
                        .thenComparing(DashboardResponse.PlanStat::active, Comparator.reverseOrder()))
                .toList();
    }

    private record Teaching(List<DashboardResponse.CoachStat> coaches, List<DashboardResponse.ClassStat> classes) {}

    /** Classes already finished in the period: per coach and per class name, confirmed places vs capacity. */
    private Teaching teaching(LocalDate from, LocalDate today, LocalDateTime now, List<Coach> coachList) {
        Map<Long, Integer> confirmed = new HashMap<>();
        for (Object[] r : bookings.counts(from, today, BookingStatus.CANCELLED))
            if (r[1] == BookingStatus.CONFIRMED) confirmed.put((Long) r[0], ((Long) r[2]).intValue());

        Map<Long, int[]> byCoach = new HashMap<>();            // [sessions, confirmed, capacity]
        Map<String, int[]> byClass = new LinkedHashMap<>();
        for (ClassSession s : sessions.findInRange(from, today)) {
            boolean over = LocalDateTime.of(s.getSessionDate(), s.getEndTime()).isBefore(now);
            if (s.getStatus() != SessionStatus.SCHEDULED || !over) continue;
            int c = confirmed.getOrDefault(s.getId(), 0);
            add(byCoach.computeIfAbsent(s.getCoach().getId(), k -> new int[3]), c, s.getCapacity());
            add(byClass.computeIfAbsent(s.getName(), k -> new int[3]), c, s.getCapacity());
        }

        var coachStats = coachList.stream().map(c -> {
            int[] t = byCoach.getOrDefault(c.getId(), new int[3]);
            return new DashboardResponse.CoachStat(c.getId(), c.getUser().getPrenom() + " " + c.getUser().getNom(),
                    c.getSpecialties(), members.countByCoachId(c.getId()), t[0], pct(t[1], t[2]));
        }).sorted(Comparator.comparingLong(DashboardResponse.CoachStat::clients).reversed()).toList();

        var classStats = byClass.entrySet().stream()
                .map(e -> new DashboardResponse.ClassStat(e.getKey(), e.getValue()[0], e.getValue()[1], e.getValue()[2],
                        pct(e.getValue()[1], e.getValue()[2])))
                .sorted(Comparator.comparingInt(DashboardResponse.ClassStat::confirmed).reversed())
                .limit(5).toList();
        return new Teaching(coachStats, classStats);
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
                    lv == null ? null : ChronoUnit.DAYS.between(lv.toLocalDate(), today));   // null = never came
        }).toList();
        return new Block<>(rows, total);
    }

    private DashboardResponse.ClassStats classes(LocalDate today) {
        LocalDate to = today.plusDays(6);
        Map<Long, int[]> counts = new HashMap<>();                                   // [confirmed, waiting]
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
        return new DashboardResponse.ClassStats(list.size(), pct(confirmed, capacity), waiting, rows);
    }

    // ---------- helpers ----------
    private record Block<T>(List<T> rows, long total) {}
    private static void add(int[] a, int confirmed, int capacity) { a[0]++; a[1] += confirmed; a[2] += capacity; }
    private static int pct(long part, long whole) { return whole == 0 ? 0 : Math.round(part * 100f / whole); }
    private static LocalDate min(LocalDate a, LocalDate b) { return a.isBefore(b) ? a : b; }
    private static String name(Member m) { return m.getFirstName() + " " + m.getLastName(); }
}