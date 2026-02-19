package com.walkdoro.domain.randombox.controller;

import com.walkdoro.domain.item.Item;
import com.walkdoro.domain.randombox.dto.RandomBoxDrawRequest;
import com.walkdoro.domain.randombox.dto.RandomBoxDrawResponse;
import com.walkdoro.domain.randombox.service.RandomBoxService;
import com.walkdoro.domain.user.User;
import com.walkdoro.domain.user.UserRepository;
import com.walkdoro.global.auth.annotation.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/random-boxes")
@RequiredArgsConstructor
public class RandomBoxController {

    private final RandomBoxService randomBoxService;
    private final UserRepository userRepository;

    @PostMapping("/draw")
    public ResponseEntity<RandomBoxDrawResponse> drawRandomBox(
            @LoginUser Long userId,
            @RequestBody RandomBoxDrawRequest request) {

        List<Item> items = randomBoxService.drawBox(userId, request.getRandomBoxType(), request.getQuantity());

        // Fetch updated user points - specific logic might be needed if service doesn't
        // return user
        // But for response we need remaining points.
        // Option 1: Service returns a DTO with items and remaining points.
        // Option 2: Controller fetches user again. (Less efficient but simpler if
        // service returns List<Item>)
        // Steps: Service transaction commits. Points updated.
        // Controller fetches user.

        User user = userRepository.findById(userId).orElseThrow();

        return ResponseEntity.ok(RandomBoxDrawResponse.builder()
                .items(items)
                .remainingPoints(user.getPoint())
                .build());
    }
}
