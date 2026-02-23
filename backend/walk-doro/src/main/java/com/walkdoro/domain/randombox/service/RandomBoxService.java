package com.walkdoro.domain.randombox.service;

import com.walkdoro.domain.item.Item;
import com.walkdoro.domain.inventory.Inventory;
import com.walkdoro.domain.inventory.repository.InventoryRepository;
import com.walkdoro.domain.item.ItemGrade;
import com.walkdoro.domain.item.repository.ItemRepository;
import com.walkdoro.domain.randombox.type.RandomBoxType;
import com.walkdoro.global.util.RandomGenerator;
import com.walkdoro.domain.user.User;
import com.walkdoro.domain.user.repository.UserRepository;
import com.walkdoro.global.error.ErrorCode;
import com.walkdoro.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RandomBoxService {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final InventoryRepository inventoryRepository;
    private final RandomGenerator randomGenerator;

    @Transactional
    public List<Item> drawBox(Long userId, RandomBoxType type, int quantity) {
        User user = userRepository.findByIdWithPessimisticLock(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        long totalCost = type.getPrice() * quantity;
        user.deductPoint(totalCost);

        Map<ItemGrade, List<Item>> gradeItemsMap = new EnumMap<>(ItemGrade.class);
        Map<Item, Integer> drawnItemCounts = new HashMap<>();
        List<Item> drawnItems = new ArrayList<>();

        for (int i = 0; i < quantity; i++) {
            double rand = randomGenerator.nextDouble();
            ItemGrade grade = type.getRandomGrade(rand);

            List<Item> items = gradeItemsMap.computeIfAbsent(grade, g -> itemRepository.findAllByGrade(g));
            if (items.isEmpty()) {
                throw new BusinessException(ErrorCode.ITEM_NOT_FOUND);
            }

            int randomIndex = (int) (randomGenerator.nextDouble() * items.size());
            Item selectedItem = items.get(randomIndex);
            drawnItems.add(selectedItem);
            drawnItemCounts.merge(selectedItem, 1, Integer::sum);
        }

        List<Inventory> userInventories = inventoryRepository.findAllByUserAndItemIn(user, drawnItemCounts.keySet());
        Map<Item, Inventory> inventoryMap = userInventories.stream()
                .collect(Collectors.toMap(Inventory::getItem, inv -> inv));

        List<Inventory> inventoriesToSave = new ArrayList<>();
        for (Map.Entry<Item, Integer> entry : drawnItemCounts.entrySet()) {
            Item item = entry.getKey();
            int count = entry.getValue();
            Inventory inventory = inventoryMap.getOrDefault(item,
                    Inventory.builder().user(user).item(item).quantity(0).build());
            inventory.addQuantity(count);
            inventoriesToSave.add(inventory);
        }
        inventoryRepository.saveAll(inventoriesToSave);

        return drawnItems;
    }
}
