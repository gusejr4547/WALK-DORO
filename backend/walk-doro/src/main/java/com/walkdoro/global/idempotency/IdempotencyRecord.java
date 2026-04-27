package com.walkdoro.global.idempotency;

import java.time.LocalDateTime;

public record IdempotencyRecord(
        IdempotencyStatus status,
        String requestHash,
        Integer httpStatus,
        String responseBody,
        LocalDateTime createdAt,
        LocalDateTime expiresAt) {
}
