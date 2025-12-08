package com.example.alina.controller;

import com.example.alina.dto.user.CreateUserRequest;
import com.example.alina.dto.user.UserDto;
import com.example.alina.dto.auth.LoginRequest;
import com.example.alina.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<UserDto> registerUser(@Valid @RequestBody CreateUserRequest userDto) {
        // Проверяем, существует ли пользователь
        if (userService.existsByUsername(userDto.username())) {
            return ResponseEntity.badRequest().build();
        }
        if (userService.existsByEmail(userDto.email())) {
            return ResponseEntity.badRequest().build();
        }

        // Хэшируем пароль
        String hashedPassword = passwordEncoder.encode(userDto.password());

        // Создаем UserDto
        UserDto newUser = new UserDto();
        newUser.setUsername(userDto.username());
        newUser.setEmail(userDto.email());
        newUser.setRole("USER");  // По умолчанию роль USER

        // Вызываем сервис с двумя параметрами
        UserDto createdUser = userService.createWithPassword(newUser, hashedPassword);
        return ResponseEntity.ok(createdUser);
    }

    @PostMapping("/login")
    public ResponseEntity<String> loginUser(@Valid @RequestBody LoginRequest loginRequest) {
        // TODO: Реализовать логику аутентификации
        // Пока возвращаем заглушку
        return ResponseEntity.ok("Login successful for: " + loginRequest.getUsernameOrEmail());
    }
}
