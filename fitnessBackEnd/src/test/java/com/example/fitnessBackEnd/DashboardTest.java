package com.example.fitnessBackEnd;

import com.example.fitnessBackEnd.service.DashboardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

class DashboardTest extends AbstractIntegrationTest {

    @Autowired DashboardService dashboard;

    @Test
    void periodControlsTheSeriesAndTheHeatmapIsAWeekByDay() {
        var d = dashboard.build(30);
        assertEquals(30, d.visits().size());
        assertEquals(30, d.period().days());
        assertEquals(7, d.heatmap().length);
        assertEquals(24, d.heatmap()[0].length);
        assertEquals(4, d.memberStatus().size());          // ACTIVE, FROZEN, EXPIRED, INACTIVE
    }

    @Test
    void theAnalystStillGetsTheFourteenDaySeries() {
        assertEquals(14, dashboard.build().visits().size());
    }
}