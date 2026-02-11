package com.walkdoro.domain.stat.dto;


public record StepSyncResponse(
        Status status,
        Integer sentStepCount,
        Integer storedStepCount,
        Long rewardedPoints
) {
    public enum Status {CREATED, UPDATED, IGNORED}

    public static StepSyncResponse created(Integer sentStepCount, Integer storedStepCount, Long rewardedPoints) {
        return new StepSyncResponse(Status.CREATED, sentStepCount, storedStepCount, rewardedPoints);
    }

    public static StepSyncResponse updated(Integer sentStepCount, Integer storedStepCount, Long rewardedPoints) {
        return new StepSyncResponse(Status.UPDATED, sentStepCount, storedStepCount, rewardedPoints);
    }

    public static StepSyncResponse ignored(Integer sentStepCount, Integer storedStepCount, Long rewardedPoints) {
        return new StepSyncResponse(Status.IGNORED, sentStepCount, storedStepCount, rewardedPoints);
    }
}
