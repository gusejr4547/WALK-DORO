package com.walkdoro.domain.user.repository;

import com.walkdoro.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> findByIdWithPessimisticLock(@Param("id") Long id);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE User u SET u.point = u.point + :amount WHERE u.id = :userId")
    void updatePoint(@Param("userId") Long userId, @Param("amount") Long amount);
}
