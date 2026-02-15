package com.walkdoro.domain.reward.controller;

import com.walkdoro.domain.reward.service.RewardService;
import com.walkdoro.domain.reward.dto.RewardClaimRequest;
import com.walkdoro.domain.reward.dto.RewardClaimResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

@RequiredArgsConstructor
@RequestMapping("/api/v1/rewards")
@RestController
public class RewardController {
    private final RewardService rewardService;

    @PostMapping("/claims")
    public ResponseEntity<RewardClaimResponse> claimReward(
            @Valid @RequestBody RewardClaimRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        RewardClaimResponse response = rewardService.claimReward(userId, request);
        return ResponseEntity.ok(response);
    }
}
