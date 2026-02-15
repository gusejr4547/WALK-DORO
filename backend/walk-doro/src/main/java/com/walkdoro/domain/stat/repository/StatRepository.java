package com.walkdoro.domain.stat.repository;

import com.walkdoro.domain.stat.DailyStat;
import com.walkdoro.domain.user.User;
import java.util.List;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface StatRepository extends JpaRepository<DailyStat, Long> {

    List<DailyStat> findAllByUserAndDateBetween(User user, LocalDate startDate, LocalDate endDate);

    Optional<DailyStat> findByUserAndDate(User user, LocalDate date);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM DailyStat d WHERE d.user = :user AND d.date = :date")
    Optional<DailyStat> findByUserAndDateWithLock(@Param("user") User user, @Param("date") LocalDate date);

    @Modifying
    @Query(value = """
            INSERT INTO daily_stat (user_id, date, step_count, reward_bit_mask, created_at, modified_at)
            VALUES (:userId, :date, :steps, 0, NOW(), NOW())
            ON DUPLICATE KEY UPDATE
                step_count = IF(:steps > daily_stat.step_count, :steps, daily_stat.step_count),
                modified_at = IF(:steps > daily_stat.step_count, NOW(), daily_stat.modified_at)
            """, nativeQuery = true)
    int upsertSteps(@Param("userId") Long userId, @Param("date") LocalDate date, @Param("steps") Integer steps);
}
