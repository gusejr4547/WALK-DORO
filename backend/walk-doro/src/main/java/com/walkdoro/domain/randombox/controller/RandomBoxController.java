package com.walkdoro.domain.randombox.controller;

import com.walkdoro.domain.randombox.service.RandomBoxService;
import com.walkdoro.domain.item.Item;
import com.walkdoro.domain.randombox.dto.RandomBoxDrawRequest;
import com.walkdoro.domain.randombox.dto.RandomBoxDrawResponse;
import com.walkdoro.global.auth.dto.UserAdapter;
import com.walkdoro.global.idempotency.IdempotencyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/random-boxes")
public class RandomBoxController {

    private final RandomBoxService randomBoxService;
    private final IdempotencyService idempotencyService;

    @PostMapping("/draw")
    public ResponseEntity<RandomBoxDrawResponse> drawBox(
            @AuthenticationPrincipal UserAdapter userAdapter,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody RandomBoxDrawRequest request) {

        Long userId = userAdapter.getId();
        return idempotencyService.execute(
                userId,
                "POST",
                "/api/v1/random-boxes/draw",
                idempotencyKey,
                request,
                RandomBoxDrawResponse.class,
                () -> {
                    List<Item> drawnItems = randomBoxService.drawBox(userId, request.getType(), request.getQuantity());
                    return ResponseEntity.ok(RandomBoxDrawResponse.from(drawnItems));
                });
    }
}
