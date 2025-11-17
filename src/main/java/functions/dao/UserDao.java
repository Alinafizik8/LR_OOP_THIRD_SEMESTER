package functions.dao;

import dto.UserDTO;

import java.util.List;
import java.util.Optional;

public interface UserDao {

    // --- CRUD ---
    Long save(UserDTO dto);
    Optional<UserDTO> findById(Long id);
    Optional<UserDTO> findByEmail(String email);
    Optional<UserDTO> findByUsername(String username);
    List<UserDTO> findAll();
    void deleteById(Long id);

    // --- Аутентификация (Basic Auth) ---
    Optional<UserDTO> findByUsernameAndPassword(String username, String passwordHash);

    // --- Управление паролем ---
    void updatePassword(Long id, String newPasswordHash);

    // --- Управление ролями ---
    List<String> findAllRoles();                 // ["user", "admin"]
    List<String> findRolesByUserId(Long userId); // у конкретного user — роли
    void assignRolesToUser(Long userId, List<String> roleNames);
    void clearUserRoles(Long userId);
    void updateRole(Long id, String newRole);   // оставить для обратной совместимости

    // --- Поиск по роли (опционально) ---
    List<UserDTO> findByRole(String roleName);
}