package com.taskmanagement.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

@Getter
public class CustomUserDetails extends User {
    private final Long userId;
    private final String firstName;
    private final String lastName;

    public CustomUserDetails(Long userId, String username, String password,
                             Collection<? extends GrantedAuthority> authorities,
                             String firstName, String lastName) {
        super(username, password, authorities);
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
    }
}
