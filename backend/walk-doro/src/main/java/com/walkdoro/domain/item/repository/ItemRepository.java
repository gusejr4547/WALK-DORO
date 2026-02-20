package com.walkdoro.domain.item.repository;

import com.walkdoro.domain.item.Item;
import com.walkdoro.domain.item.ItemGrade;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findAllByGrade(ItemGrade grade);
}
