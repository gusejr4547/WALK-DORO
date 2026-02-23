package com.walkdoro.domain.inventory.repository;

import com.walkdoro.domain.inventory.Inventory;
import com.walkdoro.domain.item.Item;
import com.walkdoro.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    Optional<Inventory> findByUserAndItem(User user, Item item);

    List<Inventory> findAllByUserAndItemIn(User user, Collection<Item> items);
}
