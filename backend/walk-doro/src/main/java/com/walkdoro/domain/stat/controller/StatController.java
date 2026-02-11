package com.walkdoro.domain.stat.controller;

import com.walkdoro.domain.stat.dto.StepSyncRequest;
import com.walkdoro.domain.stat.dto.StepSyncResponse;
import com.walkdoro.domain.stat.service.StatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.attribute.UserPrincipal;

@RequiredArgsConstructor
@RestController("/api/v1/stats")
public class StatController {
    private final StatService statService;

    @PostMapping("/steps")
    public ResponseEntity<StepSyncResponse> syncSteps(@RequestBody StepSyncRequest stepSyncRequest,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long id = Long.parseLong(userDetails.getUsername());
        StepSyncResponse response = statService.syncSteps(id, stepSyncRequest);

        if (response.status() == StepSyncResponse.Status.CREATED) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/daily")
    public ResponseEntity<?> getDailyStat() {
        return ResponseEntity.ok(null);
    }

    @GetMapping("/weekly")
    public ResponseEntity<?> getWeeklyStats() {
        return ResponseEntity.ok(null);
    }
}
