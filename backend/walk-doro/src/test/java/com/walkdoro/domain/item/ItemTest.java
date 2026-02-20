package com.walkdoro.domain.item;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ItemTest {

    @Test
    @DisplayName("Item 엔티티 생성 시 ItemCategory가 올바르게 할당되어야 한다")
    void testItemCategory() {
        Item item = Item.builder()
                .name("Santa Hat")
                .grade(ItemGrade.RARE)
                .category(ItemCategory.HEADGEAR)
                .imageUrl("someUrl")
                .description("A festive hat")
                .build();

        assertThat(item.getCategory()).isEqualTo(ItemCategory.HEADGEAR);
    }
}
