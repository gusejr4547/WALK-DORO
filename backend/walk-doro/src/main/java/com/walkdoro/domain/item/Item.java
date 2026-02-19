package com.walkdoro.domain.item;

import com.walkdoro.domain.item.ItemGrade;
import com.walkdoro.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
public class Item extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemGrade grade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemCategory category;

    private String imageUrl;

    private String description;

    @Builder
    public Item(String name, ItemGrade grade, ItemCategory category, String imageUrl, String description) {
        this.name = name;
        this.grade = grade;
        this.category = category;
        this.imageUrl = imageUrl;
        this.description = description;
    }
}
