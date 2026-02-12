package com.walkdoro.domain.stat.dto;

import java.util.List;

public record StatListResponse(
        List<DailyStatResponse> dailyStats,
        Integer totalSteps,
        Long totalPoints) {
    public static StatListResponse of(List<DailyStatResponse> dailyStats) {
        int totalSteps = dailyStats.stream().mapToInt(DailyStatResponse::stepCount).sum();
        long totalPoints = dailyStats.stream().mapToLong(DailyStatResponse::rewardedPoints).sum();
        return new StatListResponse(dailyStats, totalSteps, totalPoints);
    }
}
