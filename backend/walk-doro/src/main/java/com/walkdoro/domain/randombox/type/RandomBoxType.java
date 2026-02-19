package com.walkdoro.domain.randombox.type;

import com.walkdoro.domain.item.ItemGrade;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@Getter
@RequiredArgsConstructor
public enum RandomBoxType {
    BASIC(1, Map.of(
            ItemGrade.COMMON, 0.80,
            ItemGrade.RARE, 0.15,
            ItemGrade.EPIC, 0.04,
            ItemGrade.LEGENDARY, 0.01)),
    PREMIUM(5, Map.of(
            ItemGrade.COMMON, 0.30,
            ItemGrade.RARE, 0.40,
            ItemGrade.EPIC, 0.20,
            ItemGrade.LEGENDARY, 0.10));

    private final int price;
    private final Map<ItemGrade, Double> probabilities;
}
