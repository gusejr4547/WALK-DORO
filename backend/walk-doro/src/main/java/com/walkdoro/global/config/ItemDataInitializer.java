package com.walkdoro.global.config;

import com.walkdoro.domain.item.Item;
import com.walkdoro.domain.item.ItemCategory;
import com.walkdoro.domain.item.ItemGrade;
import com.walkdoro.domain.item.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ItemDataInitializer implements CommandLineRunner {

    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (itemRepository.count() == 0) {
            log.info("Initializing item dummy data...");
            List<Item> dummyItems = Arrays.asList(
                    // Common
                    Item.builder().name("Energetic Smile").grade(ItemGrade.COMMON).category(ItemCategory.FACE).build(),
                    Item.builder().name("Red Ribbon").grade(ItemGrade.COMMON).category(ItemCategory.HEADGEAR).build(),
                    Item.builder().name("Wooden Stick").grade(ItemGrade.COMMON).category(ItemCategory.PROP).build(),
                    Item.builder().name("Park").grade(ItemGrade.COMMON).category(ItemCategory.BACKGROUND).build(),

                    // Rare
                    Item.builder().name("Crying Face").grade(ItemGrade.RARE).category(ItemCategory.FACE).build(),
                    Item.builder().name("Santa Hat").grade(ItemGrade.RARE).category(ItemCategory.HEADGEAR).build(),

                    // Epic
                    Item.builder().name("Angel Halo").grade(ItemGrade.EPIC).category(ItemCategory.HEADGEAR).build(),

                    // Legendary
                    Item.builder().name("Golden Rifle").grade(ItemGrade.LEGENDARY).category(ItemCategory.PROP).build(),
                    Item.builder().name("Space Station").grade(ItemGrade.LEGENDARY).category(ItemCategory.BACKGROUND)
                            .build());
            itemRepository.saveAll(dummyItems);
            log.info("Successfully initialized {} dummy items.", dummyItems.size());
        }
    }
}
