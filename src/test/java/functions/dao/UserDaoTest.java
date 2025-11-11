package functions.dao;

import dto.UserDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@Testcontainers
class UserDaoTest {
    private static final Logger logger = LoggerFactory.getLogger(UserDaoTest.class);

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    private DataSource dataSource;
    private UserDao dao;

    @BeforeEach
    void setUp() throws SQLException {
        logger.info("🔧 Initializing test context for UserDao");

        var config = new com.zaxxer.hikari.HikariConfig();
        config.setJdbcUrl(postgres.getJdbcUrl());
        config.setUsername(postgres.getUsername());
        config.setPassword(postgres.getPassword());
        dataSource = new com.zaxxer.hikari.HikariDataSource(config);

        // Создаём таблицу
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
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
        logger.debug("✅ DAO initialized");
    }

    @Test
    void save_and_findById_works() {
        logger.info("🧪 Test: save_and_findById_works");

        UserDTO dto = new UserDTO(
                null, "tester", "hash123", "test@example.com", "USER", null, null
        );
        Long id = dao.save(dto);
        logger.debug("💾 Saved user id={}", id);

        Optional<UserDTO> found = dao.findById(id);
        assertThat(found).isPresent();
        UserDTO u = found.get();
        assertThat(u.getId()).isEqualTo(id);
        assertThat(u.getUsername()).isEqualTo("tester");
        assertThat(u.getEmail()).isEqualTo("test@example.com");
        assertThat(u.getRole()).isEqualTo("USER");
        assertThat(u.getCreatedAt()).isNotNull();
        assertThat(u.getUpdatedAt()).isNotNull();

        logger.info("✅ Test passed");
    }

    @Test
    void unique_constraints_enforced() {
        logger.info("🧪 Test: unique_constraints_enforced");

        dao.save(new UserDTO(null, "u1", "h", "a@a.com", "USER", null, null));
        logger.debug("💾 First user saved");

        // Дубль email
        assertThatThrownBy(() ->
                dao.save(new UserDTO(null, "u2", "h", "a@a.com", "USER", null, null))
        ).isInstanceOf(RuntimeException.class)
                .hasMessageContaining("duplicate");

        // Дубль username
        assertThatThrownBy(() ->
                dao.save(new UserDTO(null, "u1", "h", "b@b.com", "USER", null, null))
        ).isInstanceOf(RuntimeException.class)
                .hasMessageContaining("duplicate");

        logger.warn("⚠️ Duplicate email/username rejected");
        logger.info("✅ Test passed");
    }

    @Test
    void findByEmail_and_findByUsername_work() {
        logger.info("🧪 Test: findByEmail_and_findByUsername_work");

        dao.save(new UserDTO(null, "xuser", "h", "x@x.com", "USER", null, null));
        logger.debug("💾 User 'xuser' created");

        assertThat(dao.findByEmail("x@x.com")).isPresent();
        assertThat(dao.findByUsername("xuser")).isPresent();
        assertThat(dao.findByEmail("missing@x.com")).isEmpty();
        assertThat(dao.findByUsername("missing")).isEmpty();

        logger.debug("✅ Email & username search works");
        logger.info("✅ Test passed");
    }

    @Test
    void updatePassword_works() {
        logger.info("🧪 Test: updatePassword_works");

        Long id = dao.save(new UserDTO(null, "p", "old", "p@p.com", "USER", null, null));
        dao.updatePassword(id, "new_hash");
        logger.debug("✏️ Updated password for user id={}", id);

        String actual = dao.findById(id).get().getPasswordHash();
        assertThat(actual).isEqualTo("new_hash");

        logger.info("✅ Test passed");
    }

    @Test
    void updateRole_works() {
        logger.info("🧪 Test: updateRole_works");

        Long id = dao.save(new UserDTO(null, "r", "h", "r@r.com", "USER", null, null));
        dao.updateRole(id, "ADMIN");
        logger.debug("✏️ Promoted user id={} to ADMIN", id);

        UserDTO u = dao.findById(id).get();
        assertThat(u.getRole()).isEqualTo("ADMIN");

        logger.info("✅ Test passed");
    }

    @Test
    void deleteById_works() {
        logger.info("🧪 Test: deleteById_works");

        Long id = dao.save(new UserDTO(null, "d", "h", "d@d.com", "USER", null, null));
        dao.deleteById(id);
        assertThat(dao.findById(id)).isEmpty();

        logger.debug("✅ Confirmed deletion");
        logger.info("✅ Test passed");
    }

    @Test
    void findAll_returns_all_users() {
        logger.info("🧪 Test: findAll_returns_all_users");

        dao.save(new UserDTO(null, "a", "h", "a@a.com", "USER", null, null));
        dao.save(new UserDTO(null, "b", "h", "b@b.com", "ADMIN", null, null));
        dao.save(new UserDTO(null, "c", "h", "c@c.com", "USER", null, null));

        List<UserDTO> list = dao.findAll();
        assertThat(list).hasSize(3);
        assertThat(list).extracting(UserDTO::getUsername).contains("a", "b", "c");

        logger.debug("✅ Loaded {} users", list.size());
        logger.info("✅ Test passed");
    }

    @Test
    void dto_constructor_handles_null_fields() {
        logger.info("🧪 Test: dto_constructor_handles_null_fields");

        // Передаём null — DAO заполняет id/время
        UserDTO dto = new UserDTO(null, "nulltest", "h", "n@n.com", "USER", null, null);
        Long id = dao.save(dto);
        UserDTO saved = dao.findById(id).get();

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
        logger.debug("✅ Auto-filled: id={}, createdAt={}, updatedAt={}",
                saved.getId(), saved.getCreatedAt(), saved.getUpdatedAt());

        logger.info("✅ Test passed");
    }
}