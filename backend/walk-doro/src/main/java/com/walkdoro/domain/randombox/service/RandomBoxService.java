package com.walkdoro.domain.randombox.service;

import com.walkdoro.domain.item.Item;
import com.walkdoro.domain.inventory.Inventory;
import com.walkdoro.domain.inventory.InventoryRepository;
import com.walkdoro.domain.item.ItemGrade;
import com.walkdoro.domain.item.ItemRepository;
import com.walkdoro.domain.randombox.type.RandomBoxType;
import com.walkdoro.global.util.RandomGenerator;
import com.walkdoro.domain.user.User;
import com.walkdoro.domain.user.UserRepository;
import com.walkdoro.global.error.ErrorCode;
import com.walkdoro.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RandomBoxService {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final InventoryRepository inventoryRepository;
    private final RandomGenerator randomGenerator;

    @Transactional
    public List<Item> drawBox(Long userId, RandomBoxType type, int quantity) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        long totalCost = type.getPrice() * quantity;
        user.deductPoint(totalCost);

        List<Item> drawnItems = new ArrayList<>();
        for (int i = 0; i < quantity; i++) {
            double rand = randomGenerator.nextDouble();
            ItemGrade grade = type.getRandomGrade(rand);

            List<Item> items = itemRepository.findAllByGrade(grade);
            if (items.isEmpty()) {
                throw new BusinessException(ErrorCode.ITEM_NOT_FOUND);
            }

            int randomIndex = (int) (randomGenerator.nextDouble() * items.size());
            Item selectedItem = items.get(randomIndex);
            drawnItems.add(selectedItem);

            Inventory inventory = inventoryRepository.findByUserAndItem(user, selectedItem)
                    .orElseGet(() -> Inventory.builder().user(user).item(selectedItem).quantity(0).build());
            inventory.addQuantity(1);
            inventoryRepository.save(inventory);
        }

        return drawnItems;
    }
}
