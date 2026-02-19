package com.walkdoro.global.config;

import com.walkdoro.domain.item.Item;
import com.walkdoro.domain.item.ItemCategory;
import com.walkdoro.domain.item.ItemGrade;
import com.walkdoro.domain.item.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class ItemDataInitializer {

    private final ItemRepository itemRepository;

    @Bean
    public CommandLineRunner initItemData() {
        return args -> {
            if (itemRepository.count() == 0) {
                List<Item> items = List.of(
                        // FACE
                        Item.builder().name("Energetic Smile").grade(ItemGrade.COMMON).category(ItemCategory.FACE)
                                .description("A smile full of energy.").build(),
                        Item.builder().name("Crying Face").grade(ItemGrade.RARE).category(ItemCategory.FACE)
                                .description("Tears of joy? Or sadness?").build(),
                        Item.builder().name("Evil Grin").grade(ItemGrade.EPIC).category(ItemCategory.FACE)
                                .description("Planning something mischievous.").build(),

                        // HEADGEAR
                        Item.builder().name("Red Ribbon").grade(ItemGrade.COMMON).category(ItemCategory.HEADGEAR)
                                .description("Simple but cute ribbon.").build(),
                        Item.builder().name("Santa Hat").grade(ItemGrade.RARE).category(ItemCategory.HEADGEAR)
                                .description("Merry Christmas!").build(),
                        Item.builder().name("Angel Halo").grade(ItemGrade.EPIC).category(ItemCategory.HEADGEAR)
                                .description("Divine aura.").build(),
                        Item.builder().name("Commander Cap").grade(ItemGrade.LEGENDARY).category(ItemCategory.HEADGEAR)
                                .description("Respect my authority.").build(),

                        // PROP
                        Item.builder().name("Wooden Stick").grade(ItemGrade.COMMON).category(ItemCategory.PROP)
                                .description("Solid oak.").build(),
                        Item.builder().name("Balloon").grade(ItemGrade.RARE).category(ItemCategory.PROP)
                                .description("Don't let it pop.").build(),
                        Item.builder().name("Golden Rifle").grade(ItemGrade.LEGENDARY).category(ItemCategory.PROP)
                                .description("Shines on the battlefield.").build(),

                        // BACKGROUND
                        Item.builder().name("Park").grade(ItemGrade.COMMON).category(ItemCategory.BACKGROUND)
                                .description("Peaceful afternoon.").build(),
                        Item.builder().name("Space Station").grade(ItemGrade.LEGENDARY)
                                .category(ItemCategory.BACKGROUND).description("Zero gravity zone.").build());
                itemRepository.saveAll(items);
            }
        };
    }
}
