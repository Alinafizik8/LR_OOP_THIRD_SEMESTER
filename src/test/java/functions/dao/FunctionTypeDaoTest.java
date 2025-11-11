package functions.dao;

import dto.FunctionTypeDTO;
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
class FunctionTypeDaoTest {
    private static final Logger logger = LoggerFactory.getLogger(FunctionTypeDaoTest.class);

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    private DataSource dataSource;
    private FunctionTypeDao dao;

    @BeforeEach
    void setUp() throws SQLException {
        logger.info("🔧 Initializing test context for FunctionTypeDao");

        var config = new com.zaxxer.hikari.HikariConfig();
        config.setJdbcUrl(postgres.getJdbcUrl());
        config.setUsername(postgres.getUsername());
        config.setPassword(postgres.getPassword());
        dataSource = new com.zaxxer.hikari.HikariDataSource(config);

        // Создаём таблицу
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("""
                CREATE TABLE function_types (
                    id BIGSERIAL PRIMARY KEY,
                    name VARCHAR(100) UNIQUE NOT NULL,
                    localized_name VARCHAR(255) NOT NULL,
                    priority INT NOT NULL DEFAULT 0,
                    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
                );
                """);
        }

        dao = new FunctionTypeDaoImpl(dataSource);
        logger.debug("✅ DAO initialized");
    }

    @Test
    void save_and_findById_works() {
        logger.info("🧪 Test: save_and_findById_works");

        // Передаём null в id/время — DAO сам заполнит
        FunctionTypeDTO dto = new FunctionTypeDTO(
                null, "SIN", "Синус", 2, null, null
        );
        Long id = dao.save(dto);
        logger.debug("💾 Saved: id={}, name='{}'", id, dto.getName());

        Optional<FunctionTypeDTO> found = dao.findById(id);
        assertThat(found).isPresent();
        FunctionTypeDTO f = found.get();
        assertThat(f.getId()).isEqualTo(id);
        assertThat(f.getName()).isEqualTo("SIN");
        assertThat(f.getLocalizedName()).isEqualTo("Синус");
        assertThat(f.getPriority()).isEqualTo(2);
        assertThat(f.getCreatedAt()).isNotNull();
        assertThat(f.getUpdatedAt()).isNotNull();
        assertThat(f.getCreatedAt()).isEqualTo(f.getUpdatedAt()); // при создании совпадают

        logger.info("✅ Test passed");
    }

    @Test
    void unique_name_constraint() {
        logger.info("🧪 Test: unique_name_constraint");

        dao.save(new FunctionTypeDTO(null, "UNIQ", "Уник", 1, null, null));
        logger.debug("💾 First type 'UNIQ' saved");

        assertThatThrownBy(() ->
                dao.save(new FunctionTypeDTO(null, "UNIQ", "Другой", 2, null, null))
        ).isInstanceOf(RuntimeException.class)
                .hasMessageContaining("duplicate");

        logger.warn("⚠️ Duplicate name rejected");
        logger.info("✅ Test passed");
    }

    @Test
    void findByName_works() {
        logger.info("🧪 Test: findByName_works");

        dao.save(new FunctionTypeDTO(null, "FIND", "Найти", 3, null, null));
        logger.debug("💾 Type 'FIND' created");

        Optional<FunctionTypeDTO> found = dao.findByName("FIND");
        assertThat(found).isPresent();
        assertThat(found.get().getLocalizedName()).isEqualTo("Найти");

        assertThat(dao.findByName("MISS")).isEmpty();
        logger.debug("✅ Name search works");

        logger.info("✅ Test passed");
    }

    @Test
    void findAllSortedByPriority_works() {
        logger.info("🧪 Test: findAllSortedByPriority_works");

        dao.save(new FunctionTypeDTO(null, "Z", "Zeta", 10, null, null));
        dao.save(new FunctionTypeDTO(null, "A", "Alpha", 1, null, null));
        dao.save(new FunctionTypeDTO(null, "B", "Beta", 1, null, null));
        logger.debug("💾 Created 3 types");

        List<FunctionTypeDTO> list = dao.findAllSortedByPriority();
        assertThat(list).extracting(FunctionTypeDTO::getName)
                .containsExactly("A", "B", "Z");

        logger.debug("✅ Sorted: {}", list.stream().map(FunctionTypeDTO::getName).toList());
        logger.info("✅ Test passed");
    }

    @Test
    void update_works_and_updates_updatedAt() {
        logger.info("🧪 Test: update_works_and_updates_updatedAt");

        Long id = dao.save(new FunctionTypeDTO(null, "OLD", "Старое", 5, null, null));
        FunctionTypeDTO before = dao.findById(id).get();
        logger.debug("💾 Created: id={}, createdAt={}, updatedAt={}",
                id, before.getCreatedAt(), before.getUpdatedAt());

        try { Thread.sleep(10); } catch (InterruptedException ignored) {}

        FunctionTypeDTO updated = new FunctionTypeDTO(
                id, "NEW", "Новое", 6, null, null
        );
        dao.update(updated);
        logger.debug("✏️ Updated name/priority");

        FunctionTypeDTO after = dao.findById(id).get();
        assertThat(after.getName()).isEqualTo("NEW");
        assertThat(after.getPriority()).isEqualTo(6);
        assertThat(after.getUpdatedAt()).isAfter(after.getCreatedAt());
        assertThat(after.getUpdatedAt()).isAfter(before.getUpdatedAt()); // изменилось!

        logger.debug("✅ Verified update: updatedAt increased");
        logger.info("✅ Test passed");
    }

    @Test
    void deleteById_works() {
        logger.info("🧪 Test: deleteById_works");

        Long id = dao.save(new FunctionTypeDTO(null, "DEL", "Удалить", 0, null, null));
        dao.deleteById(id);
        assertThat(dao.findById(id)).isEmpty();

        logger.debug("✅ Confirmed deletion");
        logger.info("✅ Test passed");
    }

    @Test
    void dto_constructor_accepts_nulls_and_dao_handles_them() {
        logger.info("🧪 Test: dto_constructor_accepts_nulls_and_dao_handles_them");

        // Передаём null — DAO не падает, БД заполняет
        FunctionTypeDTO dto = new FunctionTypeDTO(null, "TEST", "Тест", 99, null, null);
        Long id = dao.save(dto);
        FunctionTypeDTO saved = dao.findById(id).get();

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
        logger.debug("✅ Auto-filled fields: id={}, createdAt={}, updatedAt={}",
                saved.getId(), saved.getCreatedAt(), saved.getUpdatedAt());

        logger.info("✅ Test passed");
    }
}