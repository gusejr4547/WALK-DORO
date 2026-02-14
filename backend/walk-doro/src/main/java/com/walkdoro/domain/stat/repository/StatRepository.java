package com.walkdoro.domain.stat.repository;

import com.walkdoro.domain.stat.DailyStat;
import com.walkdoro.domain.user.User;
import java.util.List;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.time.LocalDate;
import java.util.Optional;

public interface StatRepository extends JpaRepository<DailyStat, Long> {

    List<DailyStat> findAllByUserAndDateBetween(User user, LocalDate startDate, LocalDate endDate);

    Optional<DailyStat> findByUserAndDate(User user, LocalDate date);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<DailyStat> findByUserAndDateWithLock(User user, LocalDate date);
}
