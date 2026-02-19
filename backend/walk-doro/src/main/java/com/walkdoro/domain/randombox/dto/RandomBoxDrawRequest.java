package com.walkdoro.domain.randombox.dto;

import com.walkdoro.domain.randombox.type.RandomBoxType;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RandomBoxDrawRequest {
    private RandomBoxType randomBoxType;
    private int quantity = 1;

    public RandomBoxDrawRequest(RandomBoxType randomBoxType, int quantity) {
        this.randomBoxType = randomBoxType;
        this.quantity = quantity;
    }
}
