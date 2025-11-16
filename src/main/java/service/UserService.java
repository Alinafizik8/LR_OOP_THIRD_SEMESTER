//// ——— Интерфейс ———
//package service;
//
//import dto.user.UserDto;
//import java.util.List;
//import java.util.Optional;
//
//public interface UserService {
//    List<UserDto> findAll();
//    Optional<UserDto> findById(Long id);
//    UserDto create(UserDto dto);
//    UserDto update(Long id, UserDto dto);
//    void delete(Long id);
//}
// package service;
//package service;
//
//import dto.user.UserDto;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//
//import java.util.List;
//import java.util.Optional;
//
//public interface UserService {
//
//    // CRUD
//    List<UserDto> findAll();
//    Page<UserDto> findAll(Pageable pageable);
//    List<UserDto> findAllSortedByCreatedAtDesc();
//    Optional<UserDto> findById(Long id);
//    Optional<UserDto> findByUsername(String username);
//    Optional<UserDto> findByEmail(String email);
//    Optional<UserDto> findByUsernameOrEmail(String param);
//
//    UserDto create(UserDto dto);
//    UserDto update(Long id, UserDto dto);
//
//    // безопасное обновление полей
//    void updatePassword(Long id, String newPasswordHash); // хеш уже захеширован!
//    void updateRole(Long id, String role);
//
//    void deleteById(Long id);
//
//    // поиск
//    List<UserDto> searchByUsernameFragment(String fragment);
//
//    // поиск по типу функции (через связь)
//    List<UserDto> findUsersByFunctionTypeId(Long typeId);
//
//    // валидация уникальности (для регистрации)
//    boolean existsByUsername(String username);
//    boolean existsByEmail(String email);
//}
// package service;
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