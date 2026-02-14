package com.walkdoro.domain.stat.dto;

public record StepSyncResponse(
        Status status,
        Integer sentStepCount,
        Integer storedStepCount,
        Long rewardBitMask) {
    public enum Status {
        CREATED, UPDATED, IGNORED
    }

    public static StepSyncResponse created(Integer sentStepCount, Integer storedStepCount, Long rewardBitMask) {
        return new StepSyncResponse(Status.CREATED, sentStepCount, storedStepCount, rewardBitMask);
    }

    public static StepSyncResponse updated(Integer sentStepCount, Integer storedStepCount, Long rewardBitMask) {
        return new StepSyncResponse(Status.UPDATED, sentStepCount, storedStepCount, rewardBitMask);
    }

    public static StepSyncResponse ignored(Integer sentStepCount, Integer storedStepCount, Long rewardBitMask) {
        return new StepSyncResponse(Status.IGNORED, sentStepCount, storedStepCount, rewardBitMask);
    }
}
