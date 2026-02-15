package com.walkdoro.domain.stat.dto;

import java.time.LocalDate;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;

public record StepSyncRequest(
        @NotNull(message = "날짜는 필수입니다.") @PastOrPresent(message = "미래의 날짜는 입력할 수 없습니다.") LocalDate date,
        @NotNull(message = "걸음 수는 필수입니다.") @PositiveOrZero(message = "걸음 수는 0 이상이어야 합니다.") Integer steps) {
}
