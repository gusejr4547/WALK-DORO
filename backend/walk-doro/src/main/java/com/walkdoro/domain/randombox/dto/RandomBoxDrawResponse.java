package com.walkdoro.domain.randombox.dto;

import com.walkdoro.domain.item.Item;
import com.walkdoro.domain.item.dto.ItemDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RandomBoxDrawResponse {

    private List<ItemDto> items;

    public static RandomBoxDrawResponse from(List<Item> items) {
        return new RandomBoxDrawResponse(
                items.stream().map(ItemDto::from).collect(Collectors.toList()));
    }
}
