package service;

import Entity.UserEntity;
import Repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true) // Все методы по умолчанию в readOnly транзакциях
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    @Override
    public Optional<UserEntity> findUserById(Long id) {
        logger.debug("Searching for user by ID: {}", id);
        Optional<UserEntity> user = userRepository.findById(id);
        if (user.isPresent()) {
            logger.debug("User found by ID: {}", id);
        } else {
            logger.info("User not found by ID: {}", id);
        }
        return user;
    }

    @Override
    public Optional<UserEntity> findUserByUsername(String username) {
        logger.debug("Searching for user by username: {}", username);
        Optional<UserEntity> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            logger.debug("User found by username: {}", username);
        } else {
            logger.info("User not found by username: {}", username);
        }
        return user;
    }

    @Override
    public Optional<UserEntity> findUserByEmail(String email) {
        logger.debug("Searching for user by email: {}", email);
        Optional<UserEntity> user = userRepository.findByEmail(email);
        if (user.isPresent()) {
            logger.debug("User found by email: {}", email);
        } else {
            logger.info("User not found by email: {}", email);
        }
        return user;
    }

    @Override
    @Transactional // Переопределяем readOnly на true для метода, который изменяет данные
    public UserEntity saveUser(UserEntity user) {
        logger.info("Saving user: {}", user.getUsername());
        UserEntity savedUser = userRepository.save(user);
        logger.info("User saved successfully with ID: {}", savedUser.getId());
        return savedUser;
    }

    @Override
    @Transactional // Переопределяем readOnly на true для метода, который изменяет данные
    public void updateUserPassword(Long id, String newPasswordHash) {
        logger.info("Updating password for user ID: {}", id);
        int updatedRows = userRepository.updatePassword(id, newPasswordHash);
        if (updatedRows == 0) {
            logger.warn("No user found to update password for ID: {}", id);
            // В реальном приложении выбросите исключение, если пользователь не найден
            // throw new EntityNotFoundException("User not found with ID: " + id);
        } else {
            logger.info("Password updated successfully for user ID: {}", id);
        }
    }

    @Override
    @Transactional // Переопределяем readOnly на true для метода, который изменяет данные
    public void updateUserRole(Long id, String newRole) {
        logger.info("Updating role for user ID: {}, new role: {}", id, newRole);
        int updatedRows = userRepository.updateRole(id, newRole);
        if (updatedRows == 0) {
            logger.warn("No user found to update role for ID: {}", id);
            // В реальном приложении выбросите исключение, если пользователь не найден
            // throw new EntityNotFoundException("User not found with ID: " + id);
        } else {
            logger.info("Role updated successfully for user ID: {}", id);
        }
    }

    @Override
    @Transactional // Переопределяем readOnly на true для метода, который изменяет данные
    public void deleteUserById(Long id) {
        logger.info("Deleting user by ID: {}", id);
        if (!userRepository.existsById(id)) {
            logger.warn("Attempt to delete non-existent user with ID: {}", id);
            // В реальном приложении выбросите исключение
            // throw new EntityNotFoundException("User not found with ID: " + id);
            return; // Или просто выйдите
        }
        userRepository.deleteById(id);
        logger.info("User deleted successfully with ID: {}", id);
    }

    @Override
    public boolean userExistsByUsername(String username) {
        logger.debug("Checking if user exists by username: {}", username);
        boolean exists = userRepository.existsByUsername(username);
        logger.debug("User with username '{}' exists: {}", username, exists);
        return exists;
    }

    @Override
    public boolean userExistsByEmail(String email) {
        logger.debug("Checking if user exists by email: {}", email);
        boolean exists = userRepository.existsByEmail(email);
        logger.debug("User with email '{}' exists: {}", email, exists);
        return exists;
    }
}