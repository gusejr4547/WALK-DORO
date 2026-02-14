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
        @UniqueConstraint(name = "uk_daily_stat_user_date", columnNames = {"user_id", "date"})
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
    private Long rewardedPoints;

    @Builder
    public DailyStat(User user, LocalDate date, Integer stepCount) {
        this.user = user;
        this.date = date;
        this.stepCount = stepCount;
        this.rewardedPoints = 0L;
    }

    public void updateStepCount(Integer stepCount) {
        this.stepCount = stepCount;
    }

    public void addRewardedPoints(Long points) {
        this.rewardedPoints += points;
    }
}
