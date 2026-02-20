package com.walkdoro.domain.item.dto;

import com.walkdoro.domain.item.Item;
import com.walkdoro.domain.item.ItemCategory;
import com.walkdoro.domain.item.ItemGrade;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ItemDto {
    private Long id;
    private String name;
    private ItemGrade grade;
    private ItemCategory category;
    private String imageUrl;
    private String description;

    public static ItemDto from(Item item) {
        return new ItemDto(
                item.getId(),
                item.getName(),
                item.getGrade(),
                item.getCategory(),
                item.getImageUrl(),
                item.getDescription());
    }
}
