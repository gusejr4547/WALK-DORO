package com.walkdoro.global.idempotency;

import com.walkdoro.global.error.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import tools.jackson.databind.ObjectMapper;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IdempotencyServiceTest {

    private final InMemoryIdempotencyStore store = new InMemoryIdempotencyStore();
    private final IdempotencyService idempotencyService = new IdempotencyService(
            store,
            new ObjectMapper(),
            Clock.fixed(Instant.parse("2026-04-26T10:00:00Z"), ZoneOffset.UTC));

    @Test
    @DisplayName("같은 키와 같은 요청은 실제 작업을 한 번만 실행하고 저장된 응답을 반환한다")
    void execute_ShouldReturnStoredResponse_WhenSameKeyAndRequestAreRetried() {
        AtomicInteger executionCount = new AtomicInteger();
        SampleRequest request = new SampleRequest("BASIC", 1);

        ResponseEntity<SampleResponse> first = idempotencyService.execute(
                1L,
                "POST",
                "/api/v1/random-boxes/draw",
                "same-key",
                request,
                SampleResponse.class,
                () -> {
                    executionCount.incrementAndGet();
                    return ResponseEntity.ok(new SampleResponse("first-result"));
                });

        ResponseEntity<SampleResponse> second = idempotencyService.execute(
                1L,
                "POST",
                "/api/v1/random-boxes/draw",
                "same-key",
                request,
                SampleResponse.class,
                () -> {
                    executionCount.incrementAndGet();
                    return ResponseEntity.ok(new SampleResponse("second-result"));
                });

        assertThat(first.getBody().value()).isEqualTo("first-result");
        assertThat(second.getBody().value()).isEqualTo("first-result");
        assertThat(executionCount).hasValue(1);
    }

    @Test
    @DisplayName("같은 키로 다른 요청 본문이 들어오면 충돌로 거부한다")
    void execute_ShouldReject_WhenSameKeyIsUsedWithDifferentRequest() {
        idempotencyService.execute(
                1L,
                "POST",
                "/api/v1/random-boxes/draw",
                "same-key",
                new SampleRequest("BASIC", 1),
                SampleResponse.class,
                () -> ResponseEntity.ok(new SampleResponse("first-result")));

        assertThatThrownBy(() -> idempotencyService.execute(
                1L,
                "POST",
                "/api/v1/random-boxes/draw",
                "same-key",
                new SampleRequest("BASIC", 2),
                SampleResponse.class,
                () -> ResponseEntity.ok(new SampleResponse("second-result"))))
                .isInstanceOf(BusinessException.class);
    }

    private record SampleRequest(String type, int quantity) {
    }

    private record SampleResponse(String value) {
    }

    private static class InMemoryIdempotencyStore implements IdempotencyStore {

        private final Map<String, IdempotencyRecord> records = new HashMap<>();

        @Override
        public Optional<IdempotencyRecord> findByKey(String key) {
            return Optional.ofNullable(records.get(key));
        }

        @Override
        public boolean saveProcessingIfAbsent(String key, IdempotencyRecord record, Duration ttl) {
            if (records.containsKey(key)) {
                return false;
            }
            records.put(key, record);
            return true;
        }

        @Override
        public void saveCompleted(String key, IdempotencyRecord record, Duration ttl) {
            records.put(key, record);
        }

        @Override
        public void delete(String key) {
            records.remove(key);
        }
    }
}
