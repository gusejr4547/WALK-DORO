package com.walkdoro.global.auth.annotation.loginUser;

import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.List;

@Getter
public class UserAdapter extends User {
    private final Long id;

    public UserAdapter(String userIdStr, String role) {
        super(userIdStr, "", List.of(new SimpleGrantedAuthority(role)));
        this.id = Long.parseLong(userIdStr);
    }
}
