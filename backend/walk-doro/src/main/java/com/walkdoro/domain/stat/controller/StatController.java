package com.walkdoro.domain.stat.controller;

import com.walkdoro.domain.stat.dto.StepSyncRequest;
import com.walkdoro.domain.stat.dto.StepSyncResponse;
import com.walkdoro.domain.stat.service.StatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import com.walkdoro.domain.stat.dto.StatListResponse;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import com.walkdoro.global.auth.annotation.loginUser.LoginUser;

@RequiredArgsConstructor
@RequestMapping("/api/v1")
@RestController()
public class StatController {
    private final StatService statService;

    @PostMapping("/stats/steps")
    public ResponseEntity<StepSyncResponse> syncSteps(
            @Valid @RequestBody StepSyncRequest stepSyncRequest,
            @LoginUser Long userId) {
        StepSyncResponse response = statService.syncSteps(userId, stepSyncRequest);

        if (response.status() == StepSyncResponse.Status.CREATED) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats")
    public ResponseEntity<StatListResponse> getStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @LoginUser Long userId) {
        StatListResponse response = statService.getStats(userId, startDate, endDate);
        return ResponseEntity.ok(response);
    }

}
