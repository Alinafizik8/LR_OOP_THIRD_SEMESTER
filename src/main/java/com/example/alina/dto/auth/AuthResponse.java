package com.example.alina.dto.auth;

import com.example.alina.dto.user.UserDto;
import lombok.Data;
import java.time.Instant;

@Data
public class AuthResponse {

    private String token;
    private long expiresIn;
    private UserDto user;
}