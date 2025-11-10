package functions.dao;

import functions.dto.TabulatedFunctionDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.junit.jupiter.TestcontainersExtension;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.assertj.core.api.Assertions.*;

@Testcontainers
@ExtendWith(TestcontainersExtension.class)
class TabulatedFunctionDaoTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    private DataSource dataSource;
    private TabulatedFunctionDao dao;

    @BeforeEach
    void setUp() throws SQLException {
        var config = new com.zaxxer.hikari.HikariConfig();
        config.setJdbcUrl(postgres.getJdbcUrl());
        config.setUsername(postgres.getUsername());
        config.setPassword(postgres.getPassword());
        dataSource = new com.zaxxer.hikari.HikariDataSource(config);

        // Создаём схему из create_tables.sql + минимум данных
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("""
                CREATE TABLE users (id BIGSERIAL PRIMARY KEY, username TEXT);
                INSERT INTO users (username) VALUES ('user1'), ('user2');
                CREATE TABLE function_types (id BIGSERIAL PRIMARY KEY, name TEXT);
                INSERT INTO function_types (name) VALUES ('TABULATED'), ('SIN');
                CREATE TABLE tabulated_functions (
                    id BIGSERIAL PRIMARY KEY,
                    owner_id BIGINT NOT NULL REFERENCES users(id),
                    function_type_id BIGINT NOT NULL REFERENCES function_types(id),
                    serialized_data BYTEA NOT NULL,
                    name TEXT NOT NULL,
                    created_at TIMESTAMP DEFAULT NOW(),
                    updated_at TIMESTAMP DEFAULT NOW()
                );
                """);
        }

        dao = new TabulatedFunctionDao(dataSource);
    }

    private byte[] randomBytes(int n) {
        byte[] b = new byte[n];
        new Random().nextBytes(b);
        return b;
    }

    // === CREATE + READ ===
    @Test
    void save_and_find_works() {
        TabulatedFunctionDTO dto = new TabulatedFunctionDTO(1L, 1L, randomBytes(100), "func1");
        Long id = dao.save(dto);

        Optional<TabulatedFunctionDTO> found = dao.findByIdAndOwnerId(id, 1L);
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("func1");
        assertThat(found.get().getSerializedData()).hasSize(100);
    }

    // === ИЗОЛЯЦИЯ ===
    @Test
    void isolation_prevents_cross_user_access() {
        TabulatedFunctionDTO dto = new TabulatedFunctionDTO(1L, 1L, randomBytes(1), "private");
        Long id = dao.save(dto);

        assertThat(dao.findByIdAndOwnerId(id, 1L)).isPresent();   // ✅ свой
        assertThat(dao.findByIdAndOwnerId(id, 2L)).isEmpty();    // ❌ чужой
    }

    // === UPDATE ===
    @Test
    void updateName_works() {
        TabulatedFunctionDTO dto = new TabulatedFunctionDTO(1L, 1L, randomBytes(1), "old");
        Long id = dao.save(dto);
        dao.updateName(id, 1L, "new");

        var f = dao.findByIdAndOwnerId(id, 1L).get();
        assertThat(f.getName()).isEqualTo("new");
    }

    @Test
    void updateDataAndName_works() {
        TabulatedFunctionDTO old = new TabulatedFunctionDTO(1L, 1L, new byte[]{1, 2}, "old");
        Long id = dao.save(old);

        TabulatedFunctionDTO updated = new TabulatedFunctionDTO();
        updated.setSerializedData(new byte[]{3, 4, 5});
        updated.setName("new");
        dao.updateDataAndName(id, 1L, updated);

        var f = dao.findByIdAndOwnerId(id, 1L).get();
        assertThat(f.getName()).isEqualTo("new");
        assertThat(f.getSerializedData()).containsExactly(3, 4, 5);
    }

    // === DELETE ===
    @Test
    void delete_works() {
        TabulatedFunctionDTO dto = new TabulatedFunctionDTO(1L, 1L, randomBytes(1), "to-delete");
        Long id = dao.save(dto);
        dao.deleteByIdAndOwnerId(id, 1L);
        assertThat(dao.findByIdAndOwnerId(id, 1L)).isEmpty();
    }

    // === LIST + ГЕНЕРАЦИЯ ===
    @Test
    void findByOwnerId_returns_only_own() {
        for (int i = 0; i < 3; i++) {
            dao.save(new TabulatedFunctionDTO(1L, 1L, randomBytes(1), "f" + i));
        }
        for (int i = 0; i < 2; i++) {
            dao.save(new TabulatedFunctionDTO(2L, 1L, randomBytes(1), "g" + i));
        }

        List<TabulatedFunctionDTO> u1 = dao.findByOwnerId(1L);
        assertThat(u1).hasSize(3);
        assertThat(u1).extracting(TabulatedFunctionDTO::getName).contains("f0", "f2");

        List<TabulatedFunctionDTO> u2 = dao.findByOwnerId(2L);
        assertThat(u2).hasSize(2);
    }

    // === СМЕШАННЫЕ ДАННЫЕ ===
    @Test
    void mixed_data_types_work() {
        dao.save(new TabulatedFunctionDTO(1L, 1L, new byte[]{}, ""));                          // пустое
        dao.save(new TabulatedFunctionDTO(1L, 2L, new byte[]{(byte) 0xFF, 0, 127}, "sin"));     // разные байты
        dao.save(new TabulatedFunctionDTO(1L, 1L, "тест".getBytes(), "кириллица"));             // UTF-8

        List<TabulatedFunctionDTO> list = dao.findByOwnerId(1L);
        assertThat(list).hasSize(3);
    }
}
