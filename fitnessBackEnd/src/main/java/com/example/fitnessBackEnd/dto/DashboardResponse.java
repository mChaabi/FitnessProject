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
        LocalDateTime generatedAt) {

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
}