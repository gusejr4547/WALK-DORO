package com.walkdoro.global.idempotency;

import java.time.Duration;
import java.util.Optional;

public interface IdempotencyStore {

    Optional<IdempotencyRecord> findByKey(String key);

    boolean saveProcessingIfAbsent(String key, IdempotencyRecord record, Duration ttl);

    void saveCompleted(String key, IdempotencyRecord record, Duration ttl);

    void delete(String key);
}
