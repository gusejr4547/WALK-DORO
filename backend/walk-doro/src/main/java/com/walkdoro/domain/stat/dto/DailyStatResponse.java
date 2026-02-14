package com.walkdoro.domain.stat.dto;

import com.walkdoro.domain.stat.DailyStat;
import java.time.LocalDate;

public record DailyStatResponse(
        LocalDate date,
        Integer stepCount,
        Long rewardBitMask) {
    public static DailyStatResponse from(DailyStat dailyStat) {
        return new DailyStatResponse(
                dailyStat.getDate(),
                dailyStat.getStepCount(),
                dailyStat.getRewardBitMask());
    }

    public long getRewardedPoints() {
        long points = 0;
        // 1000~10000 (10 steps): 1 point each (bits 0-9)
        for (int i = 0; i < 10; i++) {
            if ((this.rewardBitMask & (1L << i)) != 0) {
                points += 1;
            }
        }
        // 12000~20000 (5 steps): 1 point each (bits 10-14)
        for (int i = 10; i < 15; i++) {
            if ((this.rewardBitMask & (1L << i)) != 0) {
                points += 1;
            }
        }
        return points;
    }
}
