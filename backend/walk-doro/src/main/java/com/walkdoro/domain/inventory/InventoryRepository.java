package com.walkdoro.domain.inventory;

import com.walkdoro.domain.item.Item;
import com.walkdoro.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    Optional<Inventory> findByUserAndItem(User user, Item item);
}
