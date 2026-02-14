package com.walkdoro.domain.reward.dto;

import java.time.LocalDate;

public record RewardClaimRequest(
                LocalDate date,
                Integer goalSteps) {
}
