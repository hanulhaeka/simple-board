package com.example.simple_board.auth;

import com.example.simple_board.model.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SessionUser {
    private Long userId;
    private Role role;
}
