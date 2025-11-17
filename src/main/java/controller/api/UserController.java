package controller.api;

import dto.user.CreateUserRequest;
import dto.user.UserDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // GET /api/v1/users
    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(userService.findAll());
    }

    // GET /api/v1/users/page?page=0&size=10&sort=createdAt,desc
    @GetMapping("/page")
    public ResponseEntity<Page<UserDto>> getUsersPage(Pageable pageable) {
        return ResponseEntity.ok(userService.findAll(pageable));
    }

    // GET /api/v1/users/sorted
    @GetMapping("/sorted")
    public ResponseEntity<List<UserDto>> getUsersSortedByCreatedAt() {
        return ResponseEntity.ok(userService.findAllSortedByCreatedAtDesc());
    }

    // GET /api/v1/users/1
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        return userService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/v1/users/by-username/{username}
    @GetMapping("/by-username/{username}")
    public ResponseEntity<UserDto> getUserByUsername(@PathVariable String username) {
        return userService.findByUsername(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/v1/users/by-email/{email}
    @GetMapping("/by-email/{email}")
    public ResponseEntity<UserDto> getUserByEmail(@PathVariable String email) {
        return userService.findByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/v1/users/by-param/{param}
    @GetMapping("/by-param/{param}")
    public ResponseEntity<UserDto> getUserByUsernameOrEmail(@PathVariable String param) {
        return userService.findByUsernameOrEmail(param)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/v1/users/search?q=john
    @GetMapping("/search")
    public ResponseEntity<List<UserDto>> searchUsers(@RequestParam String q) {
        return ResponseEntity.ok(userService.searchByUsernameFragment(q));
    }

    // GET /api/v1/users/by-function-type/5
    @GetMapping("/by-function-type/{typeId}")
    public ResponseEntity<List<UserDto>> getUsersByFunctionType(@PathVariable Long typeId) {
        return ResponseEntity.ok(userService.findUsersByFunctionTypeId(typeId));
    }

    // POST /api/v1/users
    @PostMapping
    public ResponseEntity<UserDto> createUser(@RequestBody CreateUserRequest req) {
        if (userService.existsByUsername(req.getUsername())) {
            throw new IllegalArgumentException("Username taken");
        }
        if (userService.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Email taken");
        }

        String hashed = passwordEncoder.encode(req.getPassword());
        UserDto dto = new UserDto();
        dto.setUsername(req.getUsername());
        dto.setEmail(req.getEmail());
        dto.setRole("USER");

        UserDto created = userService.createWithPassword(dto, hashed);
        logger.info("User registered: ID={}, username='{}'", created.getId(), created.getUsername());
        return ResponseEntity.status(201).body(created);
    }

    private final PasswordEncoder passwordEncoder;

    public UserController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    // PUT /api/v1/users/1
    @PutMapping("/{id}")
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
    public ResponseEntity<Void> updateRole(
            @PathVariable Long id,
            @RequestBody RoleUpdateRequest request) {
        userService.updateRole(id, request.getRole());
        logger.info("The role for the user with ID {} has been successfully updated", id);
        return ResponseEntity.noContent().build();
    }

    // DELETE /api/v1/users/1
    @DeleteMapping("/{id}")
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
