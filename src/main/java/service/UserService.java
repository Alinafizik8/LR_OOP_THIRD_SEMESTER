package service;

import dto.user.UserDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface UserService {

    // Basic CRUD
    List<UserDto> findAll();
    Page<UserDto> findAll(Pageable pageable);
    List<UserDto> findAllSortedByCreatedAtDesc();
    Optional<UserDto> findById(Long id);
    UserDto create(UserDto dto);
    UserDto update(Long id, UserDto dto);
    void deleteById(Long id);

    // Lookup by identity
    Optional<UserDto> findByUsername(String username);
    Optional<UserDto> findByEmail(String email);
    Optional<UserDto> findByUsernameOrEmail(String param);

    // Search
    List<UserDto> searchByUsernameFragment(String fragment);

    // Relations
    List<UserDto> findUsersByFunctionTypeId(Long typeId);

    // Safe field updates (no full replace)
    void updatePassword(Long id, String passwordHash); // предполагается, что хеш уже получен
    void updateRole(Long id, String role);

    // Validation helpers
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}