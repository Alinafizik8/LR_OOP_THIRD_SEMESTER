package functions.dao;

import dto.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDaoImpl implements UserDao {
    private final DataSource dataSource;
    private final Logger logger = LoggerFactory.getLogger(UserDaoImpl.class);

    public UserDaoImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Long save(UserDTO dto) {
        logger.info("Saving user: username='{}', email='{}'", dto.getUsername(), dto.getEmail());
        String sql = """
        INSERT INTO users (username, password_hash, email, role)
        VALUES (?, ?, ?, ?)
        """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) { // ← Получаем сгенерированный ID
            ps.setString(1, dto.getUsername());
            ps.setString(2, dto.getPasswordHash());
            ps.setString(3, dto.getEmail());
            ps.setString(4, dto.getRole());

            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new RuntimeException("Insert failed");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Long id = generatedKeys.getLong(1);
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
        logger.debug("Finding user by id={}", id);
        return findBy("id = ?", id);
    }

    @Override
    public Optional<UserDTO> findByEmail(String email) {
        logger.debug("Finding user by email='{}'", email);
        return findBy("email = ?", email);
    }

    @Override
    public Optional<UserDTO> findByUsername(String username) {
        logger.debug("Finding user by username='{}'", username);
        return findBy("username = ?", username);
    }

    private Optional<UserDTO> findBy(String condition, Object param) {
        String sql = "SELECT * FROM users WHERE " + condition;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    UserDTO dto = mapResultSet(rs);
                    logger.trace("Found: {}", dto);
                    return Optional.of(dto);
                } else {
                    logger.debug("Not found by {}: {}", condition, param);
                    return Optional.empty();
                }
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
            logger.error("Find all users failed", e);
            throw new RuntimeException("Find all failed", e);
        }
    }

    @Override
    public void updatePassword(Long id, String newPasswordHash) {
        logger.info("Updating password for user id={}", id);
        updateField("password_hash = ?", newPasswordHash, id);
    }

    @Override
    public void updateRole(Long id, String newRole) {
        logger.info("Updating role to '{}' for user id={}", newRole, id);
        updateField("role = ?", newRole, id);
    }

    private void updateField(String fieldSet, Object value, Long id) {
        String sql = "UPDATE users SET " + fieldSet + ", updated_at = NOW() WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, value);
            ps.setLong(2, id);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                logger.warn("Update skipped: user id={} not found", id);
                throw new RuntimeException("User not found");
            }
            logger.debug("Updated {} for user id={}", fieldSet.split(" = ")[0], id);
        } catch (SQLException e) {
            logger.error("Update failed for user id={}", id, e);
            throw new RuntimeException("Update failed", e);
        }
    }

    @Override
    public void deleteById(Long id) {
        logger.info("Delete user id={}", id);
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            int rows = ps.executeUpdate();
            logger.debug("Deleted {} user(s) with id={}", rows, id);
        } catch (SQLException e) {
            logger.error("Delete failed for user id={}", id, e);
            throw new RuntimeException("Delete failed", e);
        }
    }

    // ===== Вспомогательный метод =====
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