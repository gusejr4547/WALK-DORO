package com.walkdoro.domain.randombox.type;

import com.walkdoro.domain.item.ItemGrade;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RandomBoxType {
    BASIC(1L) {
        @Override
        public ItemGrade getRandomGrade(double rand) {
            if (rand < 0.50)
                return ItemGrade.COMMON;
            if (rand < 0.80)
                return ItemGrade.RARE;
            if (rand < 0.95)
                return ItemGrade.EPIC;
            return ItemGrade.LEGENDARY;
        }
    },
    PREMIUM(5L) {
        @Override
        public ItemGrade getRandomGrade(double rand) {
            if (rand < 0.40)
                return ItemGrade.RARE;
            if (rand < 0.80)
                return ItemGrade.EPIC;
            return ItemGrade.LEGENDARY;
        }
    };

    private final long price;

    public abstract ItemGrade getRandomGrade(double rand);
}
