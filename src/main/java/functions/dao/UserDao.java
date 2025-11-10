package functions.dao;

import functions.dto.UserDTO;

import java.util.List;
import java.util.Optional;

public interface UserDao {
    Long save(UserDTO user);
    Optional<UserDTO> findById(Long id);
    Optional<UserDTO> findByUsername(String username);
    Optional<UserDTO> findByEmail(String email);
    List<UserDTO> findAll();
    void updatePassword(Long id, String newPasswordHash);
    void updateRole(Long id, String newRole);
    void deleteById(Long id);
}