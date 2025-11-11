package functions.dao;

import dto.UserDTO;

import java.util.List;
import java.util.Optional;

/**
 * DAO для таблицы users.
 * Поддерживает CRUD, поиск по email/username, обновление пароля и роли.
 */
public interface UserDao {

    /**
     * Сохраняет нового пользователя.
     * @param dto — DTO с данными (id, createdAt, updatedAt могут быть null)
     * @return id сохранённого пользователя
     */
    Long save(UserDTO dto);

    /**
     * Находит пользователя по ID.
     */
    Optional<UserDTO> findById(Long id);

    /**
     * Находит пользователя по уникальному email.
     */
    Optional<UserDTO> findByEmail(String email);

    /**
     * Находит пользователя по уникальному username.
     */
    Optional<UserDTO> findByUsername(String username);

    /**
     * Возвращает всех пользователей (например, для админа).
     */
    List<UserDTO> findAll();

    /**
     * Обновляет пароль (хэш) пользователя.
     */
    void updatePassword(Long id, String newPasswordHash);

    /**
     * Обновляет роль пользователя (USER, ADMIN).
     */
    void updateRole(Long id, String newRole);

    /**
     * Удаляет пользователя по ID.
     */
    void deleteById(Long id);
}