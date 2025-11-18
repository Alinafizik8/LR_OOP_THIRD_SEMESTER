package functions.dao;

import dto.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class UserDaoImpl implements UserDao {
    private final DataSource dataSource;
    private final Logger logger = LoggerFactory.getLogger(UserDaoImpl.class);

    public UserDaoImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // region --- CRUD ---

    @Override
    public Long save(UserDTO dto) {
        logger.info("Saving user: username='{}', email='{}', role='{}'", dto.getUsername(), dto.getEmail(), dto.getRole());
        String sql = """
            INSERT INTO users (username, password_hash, email, role)
            VALUES (?, ?, ?, ?)
            """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, dto.getUsername());
            ps.setString(2, dto.getPasswordHash());
            ps.setString(3, dto.getEmail());
            ps.setString(4, dto.getRole());

            int rows = ps.executeUpdate();
            if (rows == 0) throw new RuntimeException("Insert failed");

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    Long id = keys.getLong(1);
                    logger.debug("Saved: id={}, username='{}'", id, dto.getUsername());
                    return id;
                }
            }
        } catch (SQLException e) {
            logger.error("Save failed for user '{}'", dto.getUsername(), e);
            throw new RuntimeException("Failed to save user", e);
        }
        throw new RuntimeException("Insert returned no ID");
    }

    @Override
    public Optional<UserDTO> findById(Long id) {
        return findBy("id = ?", id);
    }

    @Override
    public Optional<UserDTO> findByEmail(String email) {
        return findBy("email = ?", email);
    }

    @Override
    public Optional<UserDTO> findByUsername(String username) {
        return findBy("username = ?", username);
    }

    @Override
    public Optional<UserDTO> findByUsernameAndPassword(String username, String passwordHash) {
        logger.debug("Authenticating user '{}'", username);
        String sql = "SELECT * FROM users WHERE username = ? AND password_hash = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, passwordHash);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    UserDTO user = mapResultSet(rs);
                    logger.info("Authentication success: user='{}', role='{}'", username, user.getRole());
                    return Optional.of(user);
                } else {
                    logger.warn("Auth failed: invalid credentials for '{}'", username);
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            logger.error("Auth check failed for '{}'", username, e);
            throw new RuntimeException("Auth failed", e);
        }
    }

    private Optional<UserDTO> findBy(String condition, Object param) {
        String sql = "SELECT * FROM users WHERE " + condition;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            logger.error("Find by {} = {} failed", condition, param, e);
            throw new RuntimeException("Find failed", e);
        }
    }

    @Override
    public List<UserDTO> findAll() {
        logger.debug("Loading all users");
        String sql = "SELECT * FROM users ORDER BY created_at DESC";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<UserDTO> list = new ArrayList<>();
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
            logger.info("Loaded {} users", list.size());
            return list;
        } catch (SQLException e) {
            logger.error("Find all failed", e);
            throw new RuntimeException("Find all failed", e);
        }
    }

    @Override
    public List<UserDTO> findByRole(String roleName) {
        logger.debug("Finding users by role='{}'", roleName);
        String sql = "SELECT * FROM users WHERE role = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, roleName);
            try (ResultSet rs = ps.executeQuery()) {
                List<UserDTO> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
                logger.info("Found {} user(s) with role '{}'", list.size(), roleName);
                return list;
            }
        } catch (SQLException e) {
            logger.error("Find by role '{}' failed", roleName, e);
            throw new RuntimeException("Find by role failed", e);
        }
    }

    // endregion

    // region --- Role Management (упрощённый вариант: 1 роль = 1 строка) ---

    @Override
    public List<String> findAllRoles() {
        // В упрощённой модели роли хранятся в поле `role`, нет отдельной таблицы.
        // Поэтому возвращаем фиксированный список — или можно SELECT DISTINCT role FROM users
        return Arrays.asList("user", "admin");
    }

    @Override
    public List<String> findRolesByUserId(Long userId) {
        // Возвращает список из 1 элемента — текущей роли
        return findById(userId)
                .map(user -> Arrays.asList(user.getRole()))
                .orElse(Collections.emptyList());
    }

    @Override
    public void assignRolesToUser(Long userId, List<String> roleNames) {
        if (roleNames == null || roleNames.isEmpty()) {
            logger.warn("assignRolesToUser: empty role list for user id={}", userId);
            return;
        }
        String newRole = roleNames.get(0); // берём первую роль (остальные игнорируем)
        updateRole(userId, newRole);
    }

    @Override
    public void clearUserRoles(Long userId) {
        // Нельзя оставить без роли — назначаем роль по умолчанию
        updateRole(userId, "user");
    }

    @Override
    public void updateRole(Long id, String newRole) {
        logger.info("Updating role to '{}' for user id={}", newRole, id);
        String sql = "UPDATE users SET role = ?, updated_at = NOW() WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newRole);
            ps.setLong(2, id);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                logger.warn("Update skipped: user id={} not found", id);
                throw new RuntimeException("User not found");
            }
            logger.debug("Role updated to '{}' for user id={}", newRole, id);
        } catch (SQLException e) {
            logger.error("Update role failed for user id={}", id, e);
            throw new RuntimeException("Update role failed", e);
        }
    }

    // endregion

    // region --- Password & Delete ---

    @Override
    public void updatePassword(Long id, String newPasswordHash) {
        logger.info("Updating password for user id={}", id);
        String sql = "UPDATE users SET password_hash = ?, updated_at = NOW() WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newPasswordHash);
            ps.setLong(2, id);
            int rows = ps.executeUpdate();
            if (rows == 0) throw new RuntimeException("User not found");
        } catch (SQLException e) {
            logger.error("Update password failed for user id={}", id, e);
            throw new RuntimeException("Update password failed", e);
        }
    }

    @Override
    public void deleteById(Long id) {
        logger.info("Delete user id={}", id);
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int rows = ps.executeUpdate();
            logger.debug("Deleted {} user(s) with id={}", rows, id);
        } catch (SQLException e) {
            logger.error("Delete failed for user id={}", id, e);
            throw new RuntimeException("Delete failed", e);
        }
    }

    private UserDTO mapResultSet(ResultSet rs) throws SQLException {
        return new UserDTO(
                rs.getLong("id"),
                rs.getString("username"),
                rs.getString("password_hash"),
                rs.getString("email"),
                rs.getString("role"),
                rs.getObject("created_at", LocalDateTime.class),
                rs.getObject("updated_at", LocalDateTime.class)
        );
    }

}