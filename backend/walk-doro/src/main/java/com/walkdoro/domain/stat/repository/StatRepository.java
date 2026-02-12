package com.walkdoro.domain.stat.repository;

import com.walkdoro.domain.stat.DailyStat;
import com.walkdoro.domain.user.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface StatRepository extends JpaRepository<DailyStat, Long> {
    Optional<DailyStat> findByUserAndDate(User user, LocalDate date);

    List<DailyStat> findAllByUserAndDateBetween(User user, LocalDate startDate, LocalDate endDate);
}
