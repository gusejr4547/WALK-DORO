package com.walkdoro.domain.item;

import com.walkdoro.domain.BaseTimeEntity;
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

    private String imageUrl;

    private String description;

    @Builder
    public Item(String name, ItemGrade grade, String imageUrl, String description) {
        this.name = name;
        this.grade = grade;
        this.imageUrl = imageUrl;
        this.description = description;
    }
}
