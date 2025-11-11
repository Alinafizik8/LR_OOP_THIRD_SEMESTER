package functions.dao;

import dto.UserDTO;

import java.util.List;
import java.util.Optional;


public interface UserDao {

    Long save(UserDTO dto);
    Optional<UserDTO> findById(Long id);
    Optional<UserDTO> findByEmail(String email);
    Optional<UserDTO> findByUsername(String username);
    List<UserDTO> findAll();
    void updatePassword(Long id, String newPasswordHash);
    void updateRole(Long id, String newRole);
    void deleteById(Long id);
}