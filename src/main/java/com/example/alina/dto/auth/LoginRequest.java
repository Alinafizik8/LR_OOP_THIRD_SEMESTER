package com.example.alina.dto.auth;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class LoginRequest {

    @NotBlank(message = "Логин или email не могут быть пустыми")
    private String usernameOrEmail;

    @NotBlank(message = "Пароль не может быть пустым")
    private String password;
}