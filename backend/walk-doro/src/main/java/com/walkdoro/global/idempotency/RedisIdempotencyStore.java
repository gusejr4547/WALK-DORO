package com.walkdoro.global.idempotency;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RedisIdempotencyStore implements IdempotencyStore {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public Optional<IdempotencyRecord> findByKey(String key) {
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return Optional.empty();
        }
        return Optional.of(read(value.toString()));
    }

    @Override
    public boolean saveProcessingIfAbsent(String key, IdempotencyRecord record, Duration ttl) {
        Boolean saved = redisTemplate.opsForValue().setIfAbsent(key, write(record), ttl);
        return Boolean.TRUE.equals(saved);
    }

    @Override
    public void saveCompleted(String key, IdempotencyRecord record, Duration ttl) {
        redisTemplate.opsForValue().set(key, write(record), ttl);
    }

    @Override
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    private String write(IdempotencyRecord record) {
        try {
            return objectMapper.writeValueAsString(record);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize idempotency record", e);
        }
    }

    private IdempotencyRecord read(String value) {
        try {
            return objectMapper.readValue(value, IdempotencyRecord.class);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to deserialize idempotency record", e);
        }
    }
}
