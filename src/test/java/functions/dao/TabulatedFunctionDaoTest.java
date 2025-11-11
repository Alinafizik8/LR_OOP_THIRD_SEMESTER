package functions.dao;

import dto.TabulatedFunctionDTO;
import functions.LinkedListTabulatedFunction;
import functions.TabulatedFunction;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@Testcontainers
class TabulatedFunctionDaoTest {
    private static final Logger logger = LoggerFactory.getLogger(TabulatedFunctionDaoTest.class);

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");

    private DataSource dataSource;
    private TabulatedFunctionDao dao;
    private Long userId;
    private Long tabTypeId;

    @BeforeEach
    void setUp() throws SQLException {
        logger.info("🔧 Initializing test context for TabulatedFunctionDao");

        var config = new com.zaxxer.hikari.HikariConfig();
        config.setJdbcUrl(postgres.getJdbcUrl());
        config.setUsername(postgres.getUsername());
        config.setPassword(postgres.getPassword());
        dataSource = new com.zaxxer.hikari.HikariDataSource(config);

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("""
                CREATE TABLE users (id BIGSERIAL PRIMARY KEY, username TEXT);
                INSERT INTO users (username) VALUES ('user1'), ('user2');
                CREATE TABLE function_types (id BIGSERIAL PRIMARY KEY, name TEXT);
                INSERT INTO function_types (name) VALUES ('TABULATED'), ('SIN');
                CREATE TABLE tabulated_functions (
                    id BIGSERIAL PRIMARY KEY,
                    owner_id BIGINT NOT NULL,
                    function_type_id BIGINT NOT NULL,
                    serialized_data BYTEA,
                    name TEXT NOT NULL,
                    created_at TIMESTAMP DEFAULT NOW(),
                    updated_at TIMESTAMP DEFAULT NOW()
                );
                """);
        }

        dao = new TabulatedFunctionDaoImpl(dataSource);
        userId = 1L;
        tabTypeId = 1L;
        logger.debug("✅ Test environment ready");
    }

    private TabulatedFunction createTestFunction() {
        double[] x = {0.0, 1.0, 2.0};
        double[] y = {0.0, 1.0, 4.0};
        return new LinkedListTabulatedFunction(x, y);
    }

    @Test
    void save_and_findByIdAndOwnerId_works() {
        logger.info("🧪 Test: save_and_findByIdAndOwnerId_works");

        TabulatedFunction func = createTestFunction();
        TabulatedFunctionDTO dto = new TabulatedFunctionDTO(
                null, userId, tabTypeId, func, "my_func", null, null
        );
        Long id = dao.save(dto);
        logger.debug("💾 Saved function id={}", id);

        Optional<TabulatedFunctionDTO> found = dao.findByIdAndOwnerId(id, userId);
        assertThat(found).isPresent();
        TabulatedFunctionDTO f = found.get();

        // Проверяем поля DTO
        assertThat(f.getId()).isEqualTo(id);
        assertThat(f.getName()).isEqualTo("my_func");
        assertThat(f.getOwnerId()).isEqualTo(userId);
        assertThat(f.getSerializedData()).isNotNull();
        assertThat(f.getSerializedData().length).isGreaterThan(0);

        // Проверяем десериализованную функцию
        assertThat(f.getFunction()).isNotNull();
        assertThat(f.getFunction().getCount()).isEqualTo(3);
        assertThat(f.getFunction().getX(0)).isEqualTo(0.0);
        assertThat(f.getFunction().getY(2)).isEqualTo(4.0);

        logger.info("✅ Test passed");
    }

    @Test
    void isolation_prevents_cross_access() {
        logger.info("🧪 Test: isolation_prevents_cross_access");

        TabulatedFunction func = createTestFunction();
        TabulatedFunctionDTO dto = new TabulatedFunctionDTO(null, userId, tabTypeId, func, "private", null, null);
        Long id = dao.save(dto);

        assertThat(dao.findByIdAndOwnerId(id, userId)).isPresent();   // ✅ свой
        assertThat(dao.findByIdAndOwnerId(id, 2L)).isEmpty();         // ❌ чужой

        logger.info("✅ Test passed");
    }

    @Test
    void updateName_works() {
        logger.info("🧪 Test: updateName_works");

        TabulatedFunction func = createTestFunction();
        TabulatedFunctionDTO dto = new TabulatedFunctionDTO(null, userId, tabTypeId, func, "old", null, null);
        Long id = dao.save(dto);

        dao.updateName(id, userId, "new_name");
        TabulatedFunctionDTO updated = dao.findByIdAndOwnerId(id, userId).get();
        assertThat(updated.getName()).isEqualTo("new_name");

        logger.info("✅ Test passed");
    }

    @Test
    void updateFunctionAndName_works() {
        logger.info("🧪 Test: updateFunctionAndName_works");

        TabulatedFunction func1 = createTestFunction();
        TabulatedFunctionDTO dto = new TabulatedFunctionDTO(null, userId, tabTypeId, func1, "f1", null, null);
        Long id = dao.save(dto);

        // Новая функция
        double[] x2 = {0.0, 0.5};
        double[] y2 = {1.0, 0.5};
        TabulatedFunction func2 = new LinkedListTabulatedFunction(x2, y2);
        TabulatedFunctionDTO newDto = new TabulatedFunctionDTO(null, userId, tabTypeId, func2, "f2", null, null);

        dao.updateFunctionAndName(id, userId, newDto);
        TabulatedFunctionDTO updated = dao.findByIdAndOwnerId(id, userId).get();

        assertThat(updated.getName()).isEqualTo("f2");
        assertThat(updated.getFunction().getCount()).isEqualTo(2);
        assertThat(updated.getFunction().getY(1)).isEqualTo(0.5);

        logger.info("✅ Test passed");
    }

    @Test
    void deleteByIdAndOwnerId_works() {
        logger.info("🧪 Test: deleteByIdAndOwnerId_works");

        TabulatedFunction func = createTestFunction();
        TabulatedFunctionDTO dto = new TabulatedFunctionDTO(null, userId, tabTypeId, func, "to_del", null, null);
        Long id = dao.save(dto);

        dao.deleteByIdAndOwnerId(id, userId);
        assertThat(dao.findByIdAndOwnerId(id, userId)).isEmpty();

        logger.info("✅ Test passed");
    }

    @Test
    void findByOwnerId_returns_own_functions() {
        logger.info("🧪 Test: findByOwnerId_returns_own_functions");

        dao.save(new TabulatedFunctionDTO(null, userId, tabTypeId, createTestFunction(), "f1", null, null));
        dao.save(new TabulatedFunctionDTO(null, userId, tabTypeId, createTestFunction(), "f2", null, null));
        dao.save(new TabulatedFunctionDTO(null, 2L, tabTypeId, createTestFunction(), "g1", null, null));

        List<TabulatedFunctionDTO> list = dao.findByOwnerId(userId);
        assertThat(list).hasSize(2);
        assertThat(list).extracting(TabulatedFunctionDTO::getName).contains("f1", "f2");

        logger.info("✅ Test passed");
    }

}