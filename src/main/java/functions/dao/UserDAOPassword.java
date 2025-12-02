package functions.dao;

import model.User;
import model.Role;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;

public class UserDAOPassword {
    private DataSource dataSource;

    public UserDAOPassword() {
        try {
            InitialContext ctx = new InitialContext();
            dataSource = (DataSource) ctx.lookup("java:comp/env/jdbc/LR_OOP_THIRD_SEMESTER");
        } catch (NamingException e) {
            throw new RuntimeException("Cannot find DataSource", e);
        }
    }

    public User findByUsername(String username) {
        String sql = "SELECT id, username, password_hash, enabled FROM users WHERE username = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                User user = new User();
                user.setId(rs.getLong("id"));
                user.setUsername(rs.getString("username"));
                user.setPasswordHash(rs.getString("password_hash"));
                user.setEnabled(rs.getBoolean("enabled"));
                user.setRoles(findRolesByUserId(user.getId()));
                return user;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding user", e);
        }
        return null;
    }

    public void createUser(User user, String rawPassword) {
        String insertUser = "INSERT INTO users (username, password_hash, enabled) VALUES (?, ?, ?)";
        String insertUserRole = "INSERT INTO user_roles (user_id, role_name) VALUES (?, ?)";
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stmt = conn.prepareStatement(insertUser, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, user.getUsername());
                stmt.setString(2, rawPassword); // будет заменён на хеш в service
                stmt.setBoolean(3, user.isEnabled());
                stmt.executeUpdate();

                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) {
                    long userId = keys.getLong(1);
                    user.setId(userId);

                    try (PreparedStatement roleStmt = conn.prepareStatement(insertUserRole)) {
                        for (Role role : user.getRoles()) {
                            roleStmt.setLong(1, userId);
                            roleStmt.setString(2, role.name());
                            roleStmt.addBatch();
                        }
                        roleStmt.executeBatch();
                    }
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error creating user", e);
        }
    }

    private Set<Role> findRolesByUserId(Long userId) {
        Set<Role> roles = new HashSet<>();
        String sql = "SELECT role_name FROM user_roles WHERE user_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                roles.add(Role.valueOf(rs.getString("role_name")));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching roles", e);
        }
        return roles;
    }

    public void findByUsername(User user, String rawPassword) {
    }

    // UserDAOPassword.java
    public void save(User user) {
        String sql = "INSERT INTO users (username, password_hash, role, enabled) VALUES (?, ?, ?, ?)";
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getRoles().stream().findFirst().orElse(Role.USER).name());
            ps.setBoolean(4, user.isEnabled());
            ps.executeUpdate();
        } catch (SQLException e) {
            if (e.getSQLState().equals("23505")) { // unique_violation
                throw new RuntimeException("Пользователь уже существует", e);
            }
            throw new RuntimeException("Ошибка сохранения пользователя", e);
        }
    }
}