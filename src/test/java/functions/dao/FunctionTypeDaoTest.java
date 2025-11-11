package functions.dao;

import dto.FunctionTypeDTO;
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

class FunctionTypeDaoTest {
    private static final Logger logger = LoggerFactory.getLogger(FunctionTypeDaoTest.class);

    private DataSource dataSource;
    private FunctionTypeDao dao;

    @BeforeEach
    void setUp() throws SQLException {
        logger.info("Initializing test context for FunctionTypeDao");

        var config = new com.zaxxer.hikari.HikariConfig();
        config.setJdbcUrl("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        config.setUsername("sa");
        config.setPassword("");
        dataSource = new com.zaxxer.hikari.HikariDataSource(config);

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            // Очищаем старую таблицу
            stmt.execute("DROP TABLE IF EXISTS function_types CASCADE;");

            // Создаём заново
            stmt.execute("""
            CREATE TABLE IF NOT EXISTS function_types (
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
        logger.debug("Test environment ready");
    }

    @Test
    void save_and_findById_works() {
        logger.info("Test: save_and_findById_works");

        FunctionTypeDTO dto = new FunctionTypeDTO(
                null, "SIN", "Синус", 2, null, null
        );
        Long id = dao.save(dto);
        logger.debug("Saved function type: id={}, name='{}'", id, dto.getName());

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

        logger.info("Test passed");
    }

    @Test
    void unique_name_constraint_enforced() {
        logger.info("Test: unique_name_constraint_enforced");

        // Первый тип
        dao.save(new FunctionTypeDTO(null, "UNIQ", "Уник", 1, null, null));
        logger.debug("First type 'UNIQ' saved");

        // Второй с тем же name
        try {
            dao.save(new FunctionTypeDTO(null, "UNIQ", "Другой", 2, null, null));
            fail("Expected exception for duplicate name");
        } catch (RuntimeException e) {
            assertThat(e.getMessage()).contains("Failed to save function type");
            logger.warn("Duplicate name 'UNIQ' rejected by DB constraint");
        }

        logger.info("Test passed");
    }

    @Test
    void findByName_works() {
        logger.info("Test: findByName_works");

        dao.save(new FunctionTypeDTO(null, "FIND", "Найти", 3, null, null));
        logger.debug("Type 'FIND' created");

        Optional<FunctionTypeDTO> found = dao.findByName("FIND");
        assertThat(found).isPresent();
        assertThat(found.get().getLocalizedName()).isEqualTo("Найти");

        assertThat(dao.findByName("MISS")).isEmpty();
        logger.debug("Name search: FOUND='FIND', MISSING='MISS'");

        logger.info("Test passed");
    }

    @Test
    void findAllSortedByPriority_works() {
        logger.info("Test: findAllSortedByPriority_works");

        dao.save(new FunctionTypeDTO(null, "Z", "Zeta", 10, null, null));
        dao.save(new FunctionTypeDTO(null, "A", "Alpha", 1, null, null));
        dao.save(new FunctionTypeDTO(null, "B", "Beta", 1, null, null));
        logger.debug("Created 3 function types");

        List<FunctionTypeDTO> list = dao.findAllSortedByPriority();
        assertThat(list).hasSize(3);
        List<String> names = list.stream().map(FunctionTypeDTO::getName).toList();
        assertThat(names).containsExactly("A", "B", "Z");

        logger.debug("Sorted result: {}", names);
        logger.info("Test passed");
    }

    @Test
    void update_works_and_updates_updatedAt() {
        logger.info("Test: update_works_and_updates_updatedAt");

        Long id = dao.save(new FunctionTypeDTO(null, "OLD", "Старое", 5, null, null));
        FunctionTypeDTO before = dao.findById(id).get();
        logger.debug("Created: id={}, createdAt={}, updatedAt={}",
                id, before.getCreatedAt(), before.getUpdatedAt());

        try { Thread.sleep(10); } catch (InterruptedException ignored) {}

        FunctionTypeDTO updated = new FunctionTypeDTO(
                id, "NEW", "Новое", 6, null, null
        );
        dao.update(updated);
        logger.debug("Updated name/priority");

        FunctionTypeDTO after = dao.findById(id).get();
        assertThat(after.getName()).isEqualTo("NEW");
        assertThat(after.getPriority()).isEqualTo(6);
        assertThat(after.getUpdatedAt()).isAfter(after.getCreatedAt());
        assertThat(after.getUpdatedAt()).isAfter(before.getUpdatedAt()); // изменилось!

        logger.debug("Verified update: updatedAt increased");
        logger.info("Test passed");
    }

    @Test
    void deleteById_works() {
        logger.info("Test: deleteById_works");

        Long id = dao.save(new FunctionTypeDTO(null, "DEL", "Удалить", 0, null, null));
        dao.deleteById(id);
        assertThat(dao.findById(id)).isEmpty();

        logger.debug("Confirmed deletion");
        logger.info("Test passed");
    }

    @Test
    void findAll_returns_all_types() {
        logger.info("Test: findAll_returns_all_types");

        dao.save(new FunctionTypeDTO(null, "T1", "Тип 1", 1, null, null));
        dao.save(new FunctionTypeDTO(null, "T2", "Тип 2", 2, null, null));

        List<FunctionTypeDTO> list = dao.findAll();
        assertThat(list).hasSize(2);
        assertThat(list).extracting(FunctionTypeDTO::getName).contains("T1", "T2");

        logger.debug("Loaded {} types", list.size());
        logger.info("Test passed");
    }

    @Test
    void handles_null_fields_in_constructor() {
        logger.info("Test: handles_null_fields_in_constructor");

        // Передаём null — DAO сам ставит id/время
        FunctionTypeDTO dto = new FunctionTypeDTO(null, "TEST", "Тест", 99, null, null);
        Long id = dao.save(dto);
        FunctionTypeDTO saved = dao.findById(id).get();

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
        logger.debug("Auto-filled fields: id={}, createdAt={}, updatedAt={}",
                saved.getId(), saved.getCreatedAt(), saved.getUpdatedAt());

        logger.info("Test passed");
    }
}