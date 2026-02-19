package com.walkdoro.domain.randombox.dto;

import com.walkdoro.domain.item.Item;
import com.walkdoro.domain.item.ItemCategory;
import com.walkdoro.domain.item.ItemGrade;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class RandomBoxDrawResponse {
    private List<ItemDto> items;
    private Long remainingPoints;

    @Builder
    public RandomBoxDrawResponse(List<Item> items, Long remainingPoints) {
        this.items = items.stream().map(ItemDto::new).collect(Collectors.toList());
        this.remainingPoints = remainingPoints;
    }

    @Getter
    public static class ItemDto {
        private Long id;
        private String name;
        private ItemGrade grade;
        private ItemCategory category;
        private String imageUrl;
        private String description;

        public ItemDto(Item item) {
            this.id = item.getId();
            this.name = item.getName();
            this.grade = item.getGrade();
            this.category = item.getCategory();
            this.imageUrl = item.getImageUrl();
            this.description = item.getDescription();
        }
    }
}
