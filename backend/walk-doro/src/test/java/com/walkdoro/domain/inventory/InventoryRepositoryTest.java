package com.walkdoro.domain.inventory;

import com.walkdoro.domain.item.Item;
import com.walkdoro.domain.item.ItemCategory;
import com.walkdoro.domain.item.ItemGrade;
import com.walkdoro.domain.item.ItemRepository;
import com.walkdoro.domain.user.Role;
import com.walkdoro.domain.user.User;
import com.walkdoro.domain.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class InventoryRepositoryTest {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Test
    @DisplayName("findByUserAndItem 이 사용자와 아이템으로 인벤토리를 구해야 한다")
    void testFindByUserAndItem() {
        // given
        User user = User.builder().name("Test User").email("test@test.com").role(Role.USER).build();
        userRepository.save(user);

        Item item = Item.builder().name("Test Item").grade(ItemGrade.COMMON).category(ItemCategory.FACE).build();
        itemRepository.save(item);

        Inventory inventory = Inventory.builder().user(user).item(item).quantity(1).build();
        inventoryRepository.save(inventory);

        // when
        Optional<Inventory> found = inventoryRepository.findByUserAndItem(user, item);

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getQuantity()).isEqualTo(1);
    }
}
