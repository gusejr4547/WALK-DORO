package com.walkdoro.global.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Common
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "잘못된 입력값입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C002", "서버 내부 오류가 발생했습니다."),

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "해당 ID의 사용자를 찾을 수 없습니다."),

    // Stat
    STAT_NOT_FOUND(HttpStatus.NOT_FOUND, "S001", "해당 날짜의 통계 데이터를 찾을 수 없습니다."),
    STAT_UPSERT_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S002", "통계 데이터 저장 중 오류가 발생했습니다."),
    STAT_LOOKUP_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S003", "업서트 후 데이터 조회에 실패했습니다."),

    // Reward
    REWARD_ALREADY_CLAIMED(HttpStatus.BAD_REQUEST, "R001", "이미 보상을 수령했습니다."),
    REWARD_CONDITION_NOT_MET(HttpStatus.BAD_REQUEST, "R002", "보상 수령 조건을 만족하지 못했습니다."),
    REWARD_GOAL_NOT_REACHED(HttpStatus.BAD_REQUEST, "R003", "아직 목표 걸음 수에 도달하지 못했습니다."),
    INVALID_REWARD_STEP_UNIT(HttpStatus.BAD_REQUEST, "R004", "보상 수령 단위가 올바르지 않습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
