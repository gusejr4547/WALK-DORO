package com.walkdoro.domain.reward.dto;

import java.time.LocalDate;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record RewardClaimRequest(
        @NotNull(message = "날짜는 필수입니다.") LocalDate date,
        @NotNull(message = "목표 걸음 수는 필수입니다.") @Positive(message = "목표 걸음 수는 양수여야 합니다.") Integer goalSteps) {
}
