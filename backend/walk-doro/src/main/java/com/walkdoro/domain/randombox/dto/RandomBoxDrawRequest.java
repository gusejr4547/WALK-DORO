package com.walkdoro.domain.randombox.dto;

import com.walkdoro.domain.randombox.type.RandomBoxType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class RandomBoxDrawRequest {

    @NotNull(message = "RandomBoxType is required")
    private RandomBoxType type;

    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 10, message = "Quantity must not exceed 10")
    private int quantity;
}
