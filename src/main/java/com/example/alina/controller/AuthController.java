package com.example.alina.controller;

import com.example.alina.dto.user.CreateUserRequest;
import com.example.alina.dto.user.UserDto;
import com.example.alina.dto.auth.LoginRequest;
import com.example.alina.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody CreateUserRequest userDto) {
        System.out.println("✅ Получен запрос на регистрацию: " + userDto);
        if (userDto == null) {
            System.err.println("❌ userDto is NULL!");
            return ResponseEntity.badRequest().body("Request body is empty or invalid");
        }

        try {
            if (userService.existsByUsername(userDto.username())) {
                System.out.println("⚠️ Пользователь '" + userDto.username() + "' уже существует");
                return ResponseEntity.badRequest().body(Map.of("message", "Username already taken"));
            }
            if (userService.existsByEmail(userDto.email())) {
                System.out.println("⚠️ Email '" + userDto.email() + "' уже зарегистрирован");
                return ResponseEntity.badRequest().body(Map.of("message", "Email already registered"));
            }

            String hashedPassword = passwordEncoder.encode(userDto.password());
            System.out.println("🔑 Пароль хеширован: " + (hashedPassword != null && !hashedPassword.isEmpty()));

            UserDto newUser = new UserDto();
            newUser.setUsername(userDto.username());
            newUser.setEmail(userDto.email());
            newUser.setRole("USER");

            System.out.println("👤 Создаём DTO: " + newUser);

            UserDto createdUser = userService.createWithPassword(newUser, hashedPassword);
            System.out.println("✅ Пользователь создан: ID=" + createdUser.getId());
            return ResponseEntity.ok(createdUser);
        } catch (Exception e) {
            System.err.println("💥 Ошибка при регистрации:");
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "message", e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()
            ));
        }
    }

//    @PostMapping("/register")
//    public ResponseEntity<UserDto> registerUser(@Valid @RequestBody CreateUserRequest userDto) {
//        // Проверяем, существует ли пользователь
//        if (userService.existsByUsername(userDto.username())) {
//            return ResponseEntity.badRequest().build();
//        }
//        if (userService.existsByEmail(userDto.email())) {
//            return ResponseEntity.badRequest().build();
//        }
//
//        // Хэшируем пароль
//        String hashedPassword = passwordEncoder.encode(userDto.password());
//
//        // Создаем UserDto
//        UserDto newUser = new UserDto();
//        newUser.setUsername(userDto.username());
//        newUser.setEmail(userDto.email());
//        newUser.setRole("USER");  // По умолчанию роль USER
//
//        // Вызываем сервис с двумя параметрами
//        UserDto createdUser = userService.createWithPassword(newUser, hashedPassword);
//        return ResponseEntity.ok(createdUser);
//    }

    @PostMapping("/login")
    public ResponseEntity<String> loginUser(@Valid @RequestBody LoginRequest loginRequest) {
        // TODO: Реализовать логику аутентификации
        // Пока возвращаем заглушку
        return ResponseEntity.ok("Login successful for: " + loginRequest.getUsernameOrEmail());
    }
}