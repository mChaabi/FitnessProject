package com.example.fitnessBackEnd.dto;

import java.time.LocalDateTime;
import java.util.List;

public record AnalysisReply(String summary, List<InsightView> insights, List<String> actions,
                            boolean ai, LocalDateTime generatedAt, List<VideoResponse> videos) {

    public AnalysisReply withVideos(List<VideoResponse> v) {
        return new AnalysisReply(summary, insights, actions, ai, generatedAt, v);
    }

    /** metrics = the REAL values behind the insight, taken from our data, never from the AI. */
    public record InsightView(String title, String detail, String severity, List<Metric> metrics) {}
}