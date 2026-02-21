package com.walkdoro.domain.randombox.service;

import com.walkdoro.domain.inventory.Inventory;
import com.walkdoro.domain.inventory.repository.InventoryRepository;
import com.walkdoro.domain.item.Item;
import com.walkdoro.domain.item.ItemCategory;
import com.walkdoro.domain.item.ItemGrade;
import com.walkdoro.domain.item.repository.ItemRepository;
import com.walkdoro.domain.randombox.type.RandomBoxType;
import com.walkdoro.domain.user.Role;
import com.walkdoro.domain.user.User;
import com.walkdoro.domain.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class RandomBoxConcurrencyTest {

    @Autowired
    private RandomBoxService randomBoxService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = userRepository.save(User.builder()
                .name("tester")
                .email("test@net.com")
                .role(Role.USER)
                .build());

        testUser.addPoint(100L); // Add 100 points
        userRepository.saveAndFlush(testUser);

        // Save at least one item of each grade to prevent ITEM_NOT_FOUND exception
        itemRepository.save(Item.builder().name("COMMON").category(ItemCategory.FACE).grade(ItemGrade.COMMON).build());
        itemRepository.save(Item.builder().name("RARE").category(ItemCategory.HEADGEAR).grade(ItemGrade.RARE).build());
        itemRepository.save(Item.builder().name("EPIC").category(ItemCategory.PROP).grade(ItemGrade.EPIC).build());
        itemRepository.save(
                Item.builder().name("LEGENDARY").category(ItemCategory.BACKGROUND).grade(ItemGrade.LEGENDARY).build());
    }

    @AfterEach
    void tearDown() {
        inventoryRepository.deleteAllInBatch();
        itemRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("동시에 10개의 뽑기 요청이 들어올 때 포인트가 정확하게 차감되어야 한다")
    void testConcurrencyPointDeduction() throws InterruptedException {
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    randomBoxService.drawBox(testUser.getId(), RandomBoxType.BASIC, 1);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        // BASIC box costs 1 point. 10 requests should cost 10 points. 100 - 10 = 90.
        assertThat(updatedUser.getPoint()).isEqualTo(90L);
    }

    @Test
    @DisplayName("동시에 여러 개의 뽑기 요청이 들어와 단일 아이템이 중복 획득되더라도 인벤토리 수량에 문제가 없어야 한다")
    void testConcurrencyInventoryAddition() throws InterruptedException {
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    randomBoxService.drawBox(testUser.getId(), RandomBoxType.BASIC, 1);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        // 10 times drawing should generate exactly 10 drawn items.
        // Therefore, the sum of quantities of all items in the user's inventory should
        // be exactly 10.
        List<Inventory> inventories = inventoryRepository.findAll();
        int totalQuantity = inventories.stream().mapToInt(Inventory::getQuantity).sum();
        assertThat(totalQuantity).isEqualTo(10);
    }
}
