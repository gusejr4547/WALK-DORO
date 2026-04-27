package com.walkdoro.domain.reward.controller;

import com.walkdoro.domain.reward.service.RewardService;
import com.walkdoro.domain.reward.dto.RewardClaimRequest;
import com.walkdoro.domain.reward.dto.RewardClaimResponse;
import com.walkdoro.global.idempotency.IdempotencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import com.walkdoro.global.auth.annotation.LoginUser;

@RequiredArgsConstructor
@RequestMapping("/api/v1/rewards")
@RestController
public class RewardController {
    private final RewardService rewardService;
    private final IdempotencyService idempotencyService;

    @PostMapping("/claims")
    public ResponseEntity<RewardClaimResponse> claimReward(
            @Valid @RequestBody RewardClaimRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @LoginUser Long userId) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return ResponseEntity.ok(rewardService.claimReward(userId, request));
        }

        return idempotencyService.execute(
                userId,
                "POST",
                "/api/v1/rewards/claims",
                idempotencyKey,
                request,
                RewardClaimResponse.class,
                () -> ResponseEntity.ok(rewardService.claimReward(userId, request)));
    }
}
