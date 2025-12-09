package com.example.alina.controller;

import com.example.alina.dto.user.CreateUserRequest;
import com.example.alina.dto.user.UserDto;
import com.example.alina.dto.auth.LoginRequest;
import com.example.alina.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<UserDto> registerUser(@Valid @RequestBody CreateUserRequest userDto) {
        // Проверяем уникальность
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
        newUser.setRole("USER");

        // Сохраняем пользователя
        UserDto createdUser = userService.createWithPassword(newUser, hashedPassword);
        return ResponseEntity.ok(createdUser);
    }

    @PostMapping("/login")
    public ResponseEntity<UserDto> loginUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            // Аутентификация через Spring Security
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsernameOrEmail(),
                            loginRequest.getPassword()
                    )
            );

            if (authentication.isAuthenticated()) {
                // Получаем данные пользователя
                UserDto user = userService.findByUsernameOrEmail(loginRequest.getUsernameOrEmail())
                        .orElseThrow(() -> new RuntimeException("User not found after authentication"));

                // Создаем Basic Auth токен для фронтенда
                String credentials = java.util.Base64.getEncoder().encodeToString(
                        (loginRequest.getUsernameOrEmail() + ":" + loginRequest.getPassword()).getBytes()
                );

                // Устанавливаем credentials для фронтенда
                user.setCredentials(credentials);

                // Возвращаем с Authorization header
                HttpHeaders headers = new HttpHeaders();
                headers.add(HttpHeaders.AUTHORIZATION, "Basic " + credentials);

                return ResponseEntity.ok()
                        .headers(headers)
                        .body(user);
            }

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<UserDto> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String username = authentication.getName();
        return userService.findByUsername(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
