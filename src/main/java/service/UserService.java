package service;

import Entity.UserEntity;
import java.util.Optional;

public interface UserService {

    Optional<UserEntity> findUserById(Long id);
    Optional<UserEntity> findUserByUsername(String username);
    Optional<UserEntity> findUserByEmail(String email);
    UserEntity saveUser(UserEntity user);
    void updateUserPassword(Long id, String newPasswordHash);
    void updateUserRole(Long id, String newRole);
    void deleteUserById(Long id); // Обычно администраторская функция
    boolean userExistsByUsername(String username);
    boolean userExistsByEmail(String email);
}
