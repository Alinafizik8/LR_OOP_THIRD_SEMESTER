package com.example.alina.controller.api;

import com.example.alina.dto.user.CreateUserRequest;
import com.example.alina.dto.user.UserDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.alina.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    // GET /api/v1/users
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(userService.findAll());
    }

    // GET /api/v1/users/page?page=0&size=10&sort=createdAt,desc
    @GetMapping("/page")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserDto>> getUsersPage(Pageable pageable) {
        return ResponseEntity.ok(userService.findAll(pageable));
    }

    // GET /api/v1/users/sorted
    @GetMapping("/sorted")
    @PreAuthorize("hasRole('ADMIN') or #ownerId == @userService.findUserEntityByUsername(authentication.principal.username)?.get()?.id")
    public ResponseEntity<List<UserDto>> getUsersSortedByCreatedAt() {
        return ResponseEntity.ok(userService.findAllSortedByCreatedAtDesc());
    }

    // GET /api/v1/users/1
    @GetMapping("/{id}")
    @PreAuthorize("@userService.canAccessUser(authentication, #id)")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        return userService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/v1/users/by-username/{username}
    @GetMapping("/by-username/{username}")
    @PreAuthorize("hasRole('ADMIN') or #username == authentication.principal.username")
    public ResponseEntity<UserDto> getUserByUsername(@PathVariable String username) {
        return userService.findByUsername(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/v1/users/by-email/{email}
    @GetMapping("/by-email/{email}")
    @PreAuthorize("hasRole('ADMIN') or @userService.findByEmail(#email)?.get()?.username == authentication.principal.username")
    public ResponseEntity<UserDto> getUserByEmail(@PathVariable String email) {
        return userService.findByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/v1/users/by-param/{param}
    @GetMapping("/by-param/{param}")
    @PreAuthorize("hasRole('ADMIN') or @userService.findByUsernameOrEmail(#param)?.get()?.username == authentication.principal.username")
    public ResponseEntity<UserDto> getUserByUsernameOrEmail(@PathVariable String param) {
        return userService.findByUsernameOrEmail(param)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/v1/users/search?q=john
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDto>> searchUsers(@RequestParam String q) {
        return ResponseEntity.ok(userService.searchByUsernameFragment(q));
    }

    // GET /api/v1/users/by-function-type/5
    @GetMapping("/by-function-type/{typeId}")
    @PreAuthorize("hasRole('ADMIN') or #ownerId == @userService.findUserEntityByUsername(authentication.principal.username)?.get()?.id")
    public ResponseEntity<List<UserDto>> getUsersByFunctionType(@PathVariable Long typeId) {
        return ResponseEntity.ok(userService.findUsersByFunctionTypeId(typeId));
    }

    // POST /api/v1/users
    @PostMapping
    public ResponseEntity<UserDto> createUser(@RequestBody CreateUserRequest req) {
        if (userService.existsByUsername(req.username())) {
            throw new IllegalArgumentException("Username taken");
        }
        if (userService.existsByEmail(req.email())) {
            throw new IllegalArgumentException("Email taken");
        }

        String hashed = passwordEncoder.encode(req.password());
        UserDto dto = new UserDto();
        dto.setUsername(req.username());
        dto.setEmail(req.email());
        dto.setRole("USER");

        UserDto created = userService.createWithPassword(dto, hashed);
        logger.info("User registered: ID={}, username='{}'", created.getId(), created.getUsername());
        return ResponseEntity.status(201).body(created);
    }

    // PUT /api/v1/users/1
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == @userService.findUserEntityByUsername(authentication.principal.username)?.get()?.id")
    public ResponseEntity<UserDto> updateUser(
            @PathVariable Long id,
            @RequestBody UserDto dto) {
        UserDto updated = userService.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    // PATCH /api/v1/users/1/password
    @PatchMapping("/{id}/password")
    public ResponseEntity<Void> updatePassword(
            @PathVariable Long id,
            @RequestBody PasswordUpdateRequest request) {
        userService.updatePassword(id, request.getPasswordHash());
        logger.info("Updated password for User ID={}", id);
        return ResponseEntity.noContent().build();
    }

    // PATCH /api/v1/users/1/role
    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateRole(
            @PathVariable Long id,
            @RequestBody RoleUpdateRequest request) {
        userService.updateRole(id, request.getRole());
        logger.info("The role for the user with ID {} has been successfully updated", id);
        return ResponseEntity.noContent().build();
    }

    // DELETE /api/v1/users/1
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteById(id);
        logger.info("Deleted User ID={}", id);
        return ResponseEntity.noContent().build();
    }

    // ─── Вспомогательные DTO для частичных обновлений ───────────────

    public static class PasswordUpdateRequest {
        private String passwordHash; // уже захешированный!

        public String getPasswordHash() { return passwordHash; }
        public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    }

    public static class RoleUpdateRequest {
        private String role;

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }
}
