package com.walkdoro.domain.stat.repository;

import com.walkdoro.domain.stat.DailyStat;
import com.walkdoro.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface StatRepository extends JpaRepository<DailyStat, Long> {
    Optional<DailyStat> findByUserAndDate(User user, LocalDate date);
}
