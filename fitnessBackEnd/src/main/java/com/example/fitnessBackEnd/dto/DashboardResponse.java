package com.example.fitnessBackEnd.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public record DashboardResponse(
        Kpis kpis,
        List<MonthPoint> revenue,
        List<DayPoint> visits,
        List<PlanSlice> plans,
        List<ExpiringRow> expiring,
        List<AtRiskRow> atRisk,
        ClassStats classes,
        LocalDateTime generatedAt,
        // ---- new ----
        PeriodStats period,
        List<StatusSlice> memberStatus,
        List<MonthCount> memberGrowth,
        int[][] heatmap,                       // [weekday 0=Mon..6=Sun][hour 0..23] = entries
        List<PlanStat> planStats,
        List<CoachStat> coachStats,
        List<ClassStat> topClasses) {

    public record Kpis(long activeMembers, long newThisMonth,
                       BigDecimal revenueThisMonth, BigDecimal revenueSamePeriodLastMonth, Integer revenueGrowth,
                       long entriesToday, long deniedToday, long expiringCount, long atRiskCount,
                       int expiringDays, int inactivityDays) {}

    public record MonthPoint(String month, BigDecimal amount) {}            // "2026-10"
    public record DayPoint(LocalDate day, long visits) {}
    public record PlanSlice(String plan, long count) {}
    public record ExpiringRow(Long memberId, String memberName, String phone, String plan,
                              LocalDate endDate, long daysLeft) {}
    public record AtRiskRow(Long memberId, String memberName, String phone,
                            LocalDateTime lastVisit, Long daysSince) {}
    public record SessionLoad(Long id, String name, LocalDate date, LocalTime start,
                              int confirmed, int capacity, int waiting) {}
    public record ClassStats(int sessions, int avgOccupancy, long waitingTotal, List<SessionLoad> upcoming) {}

    // ---- new ----
    public record PeriodStats(int days, LocalDate from,
                              long newMembers, long newMembersPrev,
                              BigDecimal revenue, BigDecimal revenuePrev,
                              long visits, long visitsPrev,
                              long uniqueVisitors, long uniqueVisitorsPrev,
                              long coachesActive) {}
    public record StatusSlice(String status, long count) {}
    public record MonthCount(String month, long count) {}
    public record PlanStat(String plan, long active, BigDecimal revenue) {}
    public record CoachStat(Long id, String name, String specialties, long clients, int sessions, int occupancy) {}
    public record ClassStat(String name, int sessions, int confirmed, int capacity, int occupancy) {}
}