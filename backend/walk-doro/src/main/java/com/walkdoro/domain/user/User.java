package com.walkdoro.domain.user;

import com.walkdoro.domain.user.Role;
import com.walkdoro.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    private Long point;

    @Builder
    public User(String name, String email, Role role) {
        this.name = name;
        this.email = email;
        this.role = role;
        this.point = 0L;
    }

    public User update(String name) {
        this.name = name;
        return this;
    }

    public void addPoint(Long amount) {
        this.point += amount;
    }

    public String getRoleKey() {
        return this.role.getKey();
    }
}
