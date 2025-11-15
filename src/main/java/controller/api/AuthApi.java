package controller.api;

import dto.UserResponse;
import dto.auth.AuthResponse;
import dto.auth.LoginRequest;
import dto.user.CreateUserRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/v1/auth")
public interface AuthApi {

    @PostMapping("/register")
    ResponseEntity<UserResponse> register(@RequestBody CreateUserRequest request);

    @PostMapping("/login")
    ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request);

    @GetMapping("/me")
    ResponseEntity<UserResponse> getCurrentUser();
}
