package com.walkdoro.domain.stat;

import com.walkdoro.domain.user.User;
import com.walkdoro.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(name = "uk_daily_stat_user_date", columnNames = { "user_id", "date" })
})
public class DailyStat extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private Integer stepCount;

    @Column(nullable = false)
    private Long rewardBitMask;

    @Builder
    public DailyStat(User user, LocalDate date, Integer stepCount) {
        this.user = user;
        this.date = date;
        this.stepCount = stepCount;
        this.rewardBitMask = 0L;
    }

    public void updateStepCount(Integer stepCount) {
        this.stepCount = stepCount;
    }

    public boolean isRewardClaimed(int stepGoal) {
        int bitIndex = getBitIndex(stepGoal);
        return (this.rewardBitMask & (1L << bitIndex)) != 0;
    }

    public void claimReward(int stepGoal) {
        int bitIndex = getBitIndex(stepGoal);
        this.rewardBitMask |= (1L << bitIndex);
    }

    public Long getRewardedPoints() {
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

    public Long getRewardBitMask() {
        return this.rewardBitMask;
    }

    private int getBitIndex(int stepGoal) {
        if (stepGoal <= 10000) {
            if (stepGoal % 1000 != 0 || stepGoal <= 0) {
                throw new IllegalArgumentException("Invalid step goal for <= 10000: " + stepGoal);
            }
            return (stepGoal / 1000) - 1;
        } else {
            if ((stepGoal - 10000) % 2000 != 0) {
                throw new IllegalArgumentException("Invalid step goal for > 10000: " + stepGoal);
            }
            int index = 9 + (stepGoal - 10000) / 2000;
            if (index > 14) {
                throw new IllegalArgumentException("Step goal too high: " + stepGoal);
            }
            return index;
        }
    }
}
