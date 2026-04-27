package com.walkdoro.global.idempotency;

import com.walkdoro.global.error.ErrorCode;
import com.walkdoro.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private static final Duration TTL = Duration.ofHours(24);

    private final IdempotencyStore idempotencyStore;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public <T> ResponseEntity<T> execute(
            Long userId,
            String method,
            String path,
            String idempotencyKey,
            Object request,
            Class<T> responseType,
            Supplier<ResponseEntity<T>> action) {

        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new BusinessException(ErrorCode.IDEMPOTENCY_KEY_REQUIRED);
        }

        String storeKey = buildStoreKey(userId, method, path, idempotencyKey);
        String requestHash = hash(request);
        IdempotencyRecord existing = idempotencyStore.findByKey(storeKey).orElse(null);

        if (existing != null) {
            return replayOrReject(existing, requestHash, responseType);
        }

        LocalDateTime now = LocalDateTime.now(clock);
        IdempotencyRecord processing = new IdempotencyRecord(
                IdempotencyStatus.PROCESSING,
                requestHash,
                null,
                null,
                now,
                now.plus(TTL));

        if (!idempotencyStore.saveProcessingIfAbsent(storeKey, processing, TTL)) {
            IdempotencyRecord concurrent = idempotencyStore.findByKey(storeKey)
                    .orElseThrow(() -> new BusinessException(ErrorCode.IDEMPOTENCY_REQUEST_PROCESSING));
            return replayOrReject(concurrent, requestHash, responseType);
        }

        try {
            ResponseEntity<T> response = action.get();
            IdempotencyRecord completed = new IdempotencyRecord(
                    IdempotencyStatus.COMPLETED,
                    requestHash,
                    response.getStatusCode().value(),
                    write(response.getBody()),
                    now,
                    now.plus(TTL));
            idempotencyStore.saveCompleted(storeKey, completed, TTL);
            return response;
        } catch (RuntimeException e) {
            idempotencyStore.delete(storeKey);
            throw e;
        }
    }

    private <T> ResponseEntity<T> replayOrReject(
            IdempotencyRecord record,
            String requestHash,
            Class<T> responseType) {

        if (!record.requestHash().equals(requestHash)) {
            throw new BusinessException(ErrorCode.IDEMPOTENCY_KEY_CONFLICT);
        }
        if (record.status() == IdempotencyStatus.PROCESSING) {
            throw new BusinessException(ErrorCode.IDEMPOTENCY_REQUEST_PROCESSING);
        }
        return ResponseEntity
                .status(record.httpStatus())
                .body(read(record.responseBody(), responseType));
    }

    private String buildStoreKey(Long userId, String method, String path, String idempotencyKey) {
        return "idempotency:%d:%s:%s:%s".formatted(userId, method, path, idempotencyKey);
    }

    private String hash(Object value) {
        return sha256(write(value));
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    private String write(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize idempotency payload", e);
        }
    }

    private <T> T read(String value, Class<T> responseType) {
        try {
            return objectMapper.readValue(value, responseType);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to deserialize idempotency response", e);
        }
    }
}
