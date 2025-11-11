package functions.dao;

import dto.TabulatedFunctionDTO;
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

class TabulatedFunctionDaoTest {
    private static final Logger logger = LoggerFactory.getLogger(TabulatedFunctionDaoTest.class);

    private DataSource dataSource;
    private TabulatedFunctionDao dao;
    private Long userId;
    private Long tabTypeId;

    @BeforeEach
    void setUp() throws SQLException {
        logger.info("🔧 Initializing test context for TabulatedFunctionDao");

        // H2 in-memory DB (режим PostgreSQL)
        var config = new com.zaxxer.hikari.HikariConfig();
        config.setJdbcUrl("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL");
        config.setUsername("sa");
        config.setPassword("");
        dataSource = new com.zaxxer.hikari.HikariDataSource(config);

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS tabulated_functions CASCADE");
            stmt.execute("DROP TABLE IF EXISTS function_types CASCADE");
            stmt.execute("DROP TABLE IF EXISTS users CASCADE");

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
                    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
                );
                """);
        }

        dao = new TabulatedFunctionDaoImpl(dataSource);
        userId = 1L;
        tabTypeId = 1L;
        logger.debug("✅ Test environment ready");
    }

    // Генерация тестовых данных как byte[] (мимикрия сериализованной функции)
    private byte[] generateFunctionData(int pointsCount) {
        // Простая эмуляция: [x0, y0, x1, y1, ..., xn, yn] как double[] → byte[]
        double[] data = new double[pointsCount * 2];
        for (int i = 0; i < pointsCount; i++) {
            data[i * 2] = i;              // x = 0, 1, 2, ...
            data[i * 2 + 1] = i * i;      // y = x^2
        }
        return toByteArray(data);
    }

    private byte[] toByteArray(double[] doubles) {
        byte[] bytes = new byte[doubles.length * 8];
        for (int i = 0; i < doubles.length; i++) {
            long bits = Double.doubleToLongBits(doubles[i]);
            bytes[i * 8] = (byte) (bits >> 56);
            bytes[i * 8 + 1] = (byte) (bits >> 48);
            bytes[i * 8 + 2] = (byte) (bits >> 40);
            bytes[i * 8 + 3] = (byte) (bits >> 32);
            bytes[i * 8 + 4] = (byte) (bits >> 24);
            bytes[i * 8 + 5] = (byte) (bits >> 16);
            bytes[i * 8 + 6] = (byte) (bits >> 8);
            bytes[i * 8 + 7] = (byte) (bits);
        }
        return bytes;
    }

    @Test
    void save_and_findByIdAndOwnerId_works() {
        logger.info("🧪 Test: save_and_findByIdAndOwnerId_works");

        byte[] data = generateFunctionData(3); // 3 точки: (0,0), (1,1), (2,4)
        TabulatedFunctionDTO dto = new TabulatedFunctionDTO(
                null, userId, tabTypeId, null, "my_func", null, null
        );
        dto.setSerializedData(data); // ← вручную устанавливаем байты

        Long id = dao.save(dto);
        logger.debug("💾 Saved function id={}, data size={}B", id, data.length);

        Optional<TabulatedFunctionDTO> found = dao.findByIdAndOwnerId(id, userId);
        assertThat(found).isPresent();
        TabulatedFunctionDTO f = found.get();

        assertThat(f.getId()).isEqualTo(id);
        assertThat(f.getName()).isEqualTo("my_func");
        assertThat(f.getOwnerId()).isEqualTo(userId);
        assertThat(f.getSerializedData()).isNotNull();
        assertThat(f.getSerializedData()).hasSize(data.length);
        assertThat(f.getSerializedData()).isEqualTo(data);

        logger.info("✅ Test passed");
    }

    @Test
    void isolation_prevents_cross_access() {
        logger.info("🧪 Test: isolation_prevents_cross_access");

        byte[] data = generateFunctionData(1);
        TabulatedFunctionDTO dto = new TabulatedFunctionDTO(null, userId, tabTypeId, null, "private", null, null);
        dto.setSerializedData(data);
        Long id = dao.save(dto);

        assertThat(dao.findByIdAndOwnerId(id, userId)).isPresent();   // ✅ свой
        assertThat(dao.findByIdAndOwnerId(id, 2L)).isEmpty();         // ❌ чужой

        logger.info("✅ Test passed");
    }

    @Test
    void updateName_works() {
        logger.info("🧪 Test: updateName_works");

        byte[] data = generateFunctionData(1);
        TabulatedFunctionDTO dto = new TabulatedFunctionDTO(null, userId, tabTypeId, null, "old", null, null);
        dto.setSerializedData(data);
        Long id = dao.save(dto);

        dao.updateName(id, userId, "new_name");
        TabulatedFunctionDTO updated = dao.findByIdAndOwnerId(id, userId).get();
        assertThat(updated.getName()).isEqualTo("new_name");

        logger.info("✅ Test passed");
    }

    @Test
    void updateFunctionAndName_works() {
        logger.info("🧪 Test: updateFunctionAndName_works");

        // Исходная функция: 2 точки
        byte[] oldData = generateFunctionData(2);
        TabulatedFunctionDTO dto = new TabulatedFunctionDTO(null, userId, tabTypeId, null, "f1", null, null);
        dto.setSerializedData(oldData);
        Long id = dao.save(dto);

        // Новая функция: 3 точки
        byte[] newData = generateFunctionData(3);
        TabulatedFunctionDTO newDto = new TabulatedFunctionDTO(null, userId, tabTypeId, null, "f2", null, null);
        newDto.setSerializedData(newData);

        dao.updateFunctionAndName(id, userId, newDto);
        TabulatedFunctionDTO updated = dao.findByIdAndOwnerId(id, userId).get();

        assertThat(updated.getName()).isEqualTo("f2");
        assertThat(updated.getSerializedData()).hasSize(newData.length);
        assertThat(updated.getSerializedData()).isEqualTo(newData);

        logger.info("✅ Test passed");
    }

    @Test
    void deleteByIdAndOwnerId_works() {
        logger.info("🧪 Test: deleteByIdAndOwnerId_works");

        byte[] data = generateFunctionData(1);
        TabulatedFunctionDTO dto = new TabulatedFunctionDTO(null, userId, tabTypeId, null, "to_del", null, null);
        dto.setSerializedData(data);
        Long id = dao.save(dto);

        dao.deleteByIdAndOwnerId(id, userId);
        assertThat(dao.findByIdAndOwnerId(id, userId)).isEmpty();

        logger.debug("✅ Confirmed deletion");
        logger.info("✅ Test passed");
    }

    @Test
    void findByOwnerId_returns_own_functions() {
        logger.info("🧪 Test: findByOwnerId_returns_own_functions");

        byte[] data = generateFunctionData(1);
        dao.save(createDTO(userId, tabTypeId, data, "f1"));
        dao.save(createDTO(userId, tabTypeId, data, "f2"));
        dao.save(createDTO(2L, tabTypeId, data, "g1"));

        List<TabulatedFunctionDTO> list = dao.findByOwnerId(userId);
        assertThat(list).hasSize(2);
        assertThat(list).extracting(TabulatedFunctionDTO::getName).contains("f1", "f2");

        logger.info("✅ Test passed");
    }

    @Test
    void findByOwnerIdAndTypeId_filters_correctly() {
        logger.info("🧪 Test: findByOwnerIdAndTypeId_filters_correctly");

        byte[] data = generateFunctionData(1);
        dao.save(createDTO(userId, tabTypeId, data, "tab"));
        dao.save(createDTO(userId, 2L, data, "sin")); // typeId = 2 → SIN

        List<TabulatedFunctionDTO> tabs = dao.findByOwnerIdAndTypeId(userId, tabTypeId);
        assertThat(tabs).hasSize(1);
        assertThat(tabs.get(0).getName()).isEqualTo("tab");

        logger.info("✅ Test passed");
    }

    @Test
    void handles_null_serialized_data() {
        logger.info("🧪 Test: handles_null_serialized_data");

        TabulatedFunctionDTO dto = new TabulatedFunctionDTO(null, userId, tabTypeId, null, "null_func", null, null);
        dto.setSerializedData(null); // ← явный null
        Long id = dao.save(dto);

        TabulatedFunctionDTO saved = dao.findByIdAndOwnerId(id, userId).get();
        assertThat(saved.getSerializedData()).isNull();

        logger.debug("✅ Null serialized_data saved and loaded");
        logger.info("✅ Test passed");
    }

    // Вспомогательный метод
    private TabulatedFunctionDTO createDTO(Long ownerId, Long typeId, byte[] data, String name) {
        TabulatedFunctionDTO dto = new TabulatedFunctionDTO(null, ownerId, typeId, null, name, null, null);
        dto.setSerializedData(data);
        return dto;
    }
}