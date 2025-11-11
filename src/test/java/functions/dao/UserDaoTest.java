package functions.dao;

import dto.UserDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

class UserDaoTest {
    private static final Logger logger = LoggerFactory.getLogger(UserDaoTest.class);

    private DataSource dataSource;
    private UserDao dao;

    @BeforeEach
    void setUp() throws SQLException {
        logger.info("Initializing test context for UserDao");

        // H2 in-memory DB
        var config = new com.zaxxer.hikari.HikariConfig();
        config.setJdbcUrl("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL");
        config.setUsername("sa");
        config.setPassword("");
        dataSource = new com.zaxxer.hikari.HikariDataSource(config);

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            // Удаляем, если существует — гарантируем чистоту
            stmt.execute("DROP TABLE IF EXISTS users CASCADE");

            stmt.execute("""
                CREATE TABLE users (
                    id BIGSERIAL PRIMARY KEY,
                    username VARCHAR(100) UNIQUE NOT NULL,
                    password_hash TEXT NOT NULL,
                    email VARCHAR(255) UNIQUE NOT NULL,
                    role VARCHAR(20) NOT NULL DEFAULT 'USER',
                    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
                );
                """);
        }

        dao = new UserDaoImpl(dataSource);
        logger.debug("Test environment ready");
    }

    @Test
    void save_and_findById_works() {
        logger.info("Test: save_and_findById_works");

        UserDTO dto = new UserDTO(
                null, "tester", "hash123", "test@example.com", "USER", null, null
        );
        Long id = dao.save(dto);
        logger.debug("Saved user id={}", id);

        Optional<UserDTO> found = dao.findById(id);
        assertThat(found).isPresent();
        UserDTO u = found.get();
        assertThat(u.getId()).isEqualTo(id);
        assertThat(u.getUsername()).isEqualTo("tester");
        assertThat(u.getEmail()).isEqualTo("test@example.com");
        assertThat(u.getRole()).isEqualTo("USER");
        assertThat(u.getCreatedAt()).isNotNull();
        assertThat(u.getUpdatedAt()).isNotNull();
        assertThat(u.getCreatedAt()).isEqualTo(u.getUpdatedAt()); // при создании совпадают

        logger.info("Test passed");
    }

    @Test
    void unique_constraints_enforced() {
        logger.info("Test: unique_constraints_enforced");

        dao.save(new UserDTO(null, "u1", "h", "a@a.com", "USER", null, null));
        logger.debug("First user saved");

        // Попытка дубля email
        try {
            dao.save(new UserDTO(null, "u2", "h", "a@a.com", "USER", null, null));
            fail("Expected exception for duplicate email");
        } catch (RuntimeException e) {
            assertThat(e.getCause()).isInstanceOf(SQLException.class);
            String msg = e.getCause().getMessage();
            assertThat(msg).as("Check 'UNIQUE' in error")
                    .matches(m -> m.contains("UNIQUE") || m.contains("duplicate") || m.contains("Нарушение"));
            logger.warn("Duplicate email 'a@a.com' rejected by DB");
        }

        // Попытка дубля username
        try {
            dao.save(new UserDTO(null, "u1", "h", "b@b.com", "USER", null, null));
            fail("Expected exception for duplicate username");
        } catch (RuntimeException e) {
            assertThat(e.getCause()).isInstanceOf(SQLException.class);
            String msg = e.getCause().getMessage();
            assertThat(msg).as("Check 'UNIQUE' in error")
                    .matches(m -> m.contains("UNIQUE") || m.contains("duplicate") || m.contains("Нарушение"));
            logger.warn("Duplicate username 'u1' rejected by DB");
        }

        logger.info("Test passed");
    }

    @Test
    void findByEmail_and_findByUsername_work() {
        logger.info("🧪 Test: findByEmail_and_findByUsername_work");

        dao.save(new UserDTO(null, "xuser", "h", "x@x.com", "USER", null, null));
        logger.debug("User 'xuser' created");

        assertThat(dao.findByEmail("x@x.com")).isPresent();
        assertThat(dao.findByUsername("xuser")).isPresent();
        assertThat(dao.findByEmail("missing@x.com")).isEmpty();
        assertThat(dao.findByUsername("missing")).isEmpty();

        logger.debug("Email & username search: FOUND=✓, MISSING=✗");
        logger.info("Test passed");
    }

    @Test
    void updatePassword_works() {
        logger.info("Test: updatePassword_works");

        Long id = dao.save(new UserDTO(null, "p", "old", "p@p.com", "USER", null, null));
        dao.updatePassword(id, "new_hash");
        logger.debug("Updated password for user id={}", id);

        String actual = dao.findById(id).get().getPasswordHash();
        assertThat(actual).isEqualTo("new_hash");

        logger.info("Test passed");
    }

    @Test
    void updateRole_works() {
        logger.info("Test: updateRole_works");

        Long id = dao.save(new UserDTO(null, "r", "h", "r@r.com", "USER", null, null));
        dao.updateRole(id, "ADMIN");
        logger.debug("Promoted user id={} to ADMIN", id);

        UserDTO u = dao.findById(id).get();
        assertThat(u.getRole()).isEqualTo("ADMIN");

        logger.info("Test passed");
    }

    @Test
    void deleteById_works() {
        logger.info("Test: deleteById_works");

        Long id = dao.save(new UserDTO(null, "d", "h", "d@d.com", "USER", null, null));
        dao.deleteById(id);
        assertThat(dao.findById(id)).isEmpty();

        logger.debug("Confirmed deletion");
        logger.info("Test passed");
    }

    @Test
    void handles_null_fields_in_constructor() {
        logger.info("Test: handles_null_fields_in_constructor");

        UserDTO dto = new UserDTO(null, "nulltest", "h", "n@n.com", "USER", null, null);
        Long id = dao.save(dto);
        UserDTO saved = dao.findById(id).get();

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
        assertThat(saved.getCreatedAt()).isEqualTo(saved.getUpdatedAt());

        logger.debug("Auto-filled: id={}, createdAt={}, updatedAt={}",
                saved.getId(), saved.getCreatedAt(), saved.getUpdatedAt());

        logger.info("Test passed");
    }
}