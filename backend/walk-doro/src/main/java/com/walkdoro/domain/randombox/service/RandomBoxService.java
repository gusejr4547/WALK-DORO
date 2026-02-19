package com.walkdoro.domain.randombox.service;

import com.walkdoro.domain.inventory.Inventory;
import com.walkdoro.domain.inventory.InventoryRepository;
import com.walkdoro.domain.item.Item;
import com.walkdoro.domain.item.ItemGrade;
import com.walkdoro.domain.item.ItemRepository;
import com.walkdoro.domain.randombox.type.RandomBoxType;
import com.walkdoro.domain.user.User;
import com.walkdoro.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class RandomBoxService {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final InventoryRepository inventoryRepository;

    @Transactional
    public List<Item> drawBox(Long userId, RandomBoxType type, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        int totalCost = type.getPrice() * quantity;
        user.deductPoint((long) totalCost);

        List<Item> drawnItems = new ArrayList<>();
        for (int i = 0; i < quantity; i++) {
            ItemGrade grade = selectGrade(type.getProbabilities());
            List<Item> items = itemRepository.findAllByGrade(grade);
            if (items.isEmpty()) {
                throw new IllegalStateException("No items found for grade: " + grade);
            }
            Item selectedItem = items.get(ThreadLocalRandom.current().nextInt(items.size()));
            drawnItems.add(selectedItem);

            // Allow duplicates in logic, but Inventory entity might need handling.
            // Requirement says "collect/synthesize", so duplicates are allowed.
            // We should check if inventory exists and increment quantity.
            Inventory inventory = inventoryRepository.findByUserAndItem(user, selectedItem)
                    .orElseGet(() -> Inventory.builder()
                            .user(user)
                            .item(selectedItem)
                            .quantity(0)
                            .build());
            inventory.addQuantity(1);
            inventoryRepository.save(inventory);
        }

        return drawnItems;
    }

    private ItemGrade selectGrade(Map<ItemGrade, Double> probabilities) {
        double randomValue = ThreadLocalRandom.current().nextDouble();
        double cumulativeProbability = 0.0;
        for (Map.Entry<ItemGrade, Double> entry : probabilities.entrySet()) {
            cumulativeProbability += entry.getValue();
            if (randomValue <= cumulativeProbability) {
                return entry.getKey();
            }
        }
        return ItemGrade.COMMON; // Fallback
    }
}
