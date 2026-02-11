package com.walkdoro.domain.stat.dto;

import java.time.LocalDate;

public record StepSyncRequest(LocalDate date, Integer steps) {
}
