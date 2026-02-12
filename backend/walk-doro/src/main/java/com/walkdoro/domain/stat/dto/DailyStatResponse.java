package com.walkdoro.domain.stat.dto;

import com.walkdoro.domain.stat.DailyStat;
import java.time.LocalDate;

public record DailyStatResponse(
        LocalDate date,
        Integer stepCount,
        Long rewardedPoints) {
    public static DailyStatResponse from(DailyStat dailyStat) {
        return new DailyStatResponse(
                dailyStat.getDate(),
                dailyStat.getStepCount(),
                dailyStat.getRewardedPoints());
    }
}
