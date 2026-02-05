package com.walkdoro.domain.auth.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class RefreshTokenRepository {

    private final RedisTemplate<String, Object> redisTemplate;

    // key: refreshToken, value: userId
    public void save(String refreshToken, Long userId, long duration) {
        ValueOperations<String, Object> values = redisTemplate.opsForValue();
        values.set(refreshToken, String.valueOf(userId), duration, TimeUnit.MILLISECONDS);
    }

    public Optional<Long> findUserIdByRefreshToken(String refreshToken) {
        ValueOperations<String, Object> values = redisTemplate.opsForValue();
        String userId = (String) values.get(refreshToken);
        if (userId == null) {
            return Optional.empty();
        }
        return Optional.of(Long.parseLong(userId));
    }

    public void delete(String refreshToken) {
        redisTemplate.delete(refreshToken);
    }
}
