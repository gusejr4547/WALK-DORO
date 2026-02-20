package com.walkdoro.domain.item;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ItemRepositoryTest {

    @Autowired
    private ItemRepository itemRepository;

    @Test
    @DisplayName("findAllByGrade 가 특정 Grade의 아이템 리스트를 반환할 수 있어야 한다")
    void testFindAllByGrade() {
        // given
        Item commonItem1 = Item.builder().name("Item 1").grade(ItemGrade.COMMON).category(ItemCategory.FACE).build();
        Item commonItem2 = Item.builder().name("Item 2").grade(ItemGrade.COMMON).category(ItemCategory.HEADGEAR)
                .build();
        Item rareItem = Item.builder().name("Item 3").grade(ItemGrade.RARE).category(ItemCategory.PROP).build();

        itemRepository.save(commonItem1);
        itemRepository.save(commonItem2);
        itemRepository.save(rareItem);

        // when
        List<Item> commonItems = itemRepository.findAllByGrade(ItemGrade.COMMON);

        // then
        assertThat(commonItems).hasSize(2);
        assertThat(commonItems).extracting("name").containsExactlyInAnyOrder("Item 1", "Item 2");
    }
}
