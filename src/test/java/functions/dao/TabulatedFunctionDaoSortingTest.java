package functions.dao;

import functions.dto.TabulatedFunctionDTO;
import functions.dto.UserDTO;
import functions.dto.FunctionTypeDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

@Testcontainers
class TabulatedFunctionDaoSortingTest {

    private static final Logger logger = LoggerFactory.getLogger(TabulatedFunctionDaoSortingTest.class);

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    private DataSource dataSource;
    private TabulatedFunctionDao dao;
    private UserDao userDao;
    private FunctionTypeDao typeDao;

    @BeforeEach
    void setUp() throws Exception {
        logger.info("Инициализация БД для тестов сортировки");

        var config = new com.zaxxer.hikari.HikariConfig();
        config.setJdbcUrl(postgres.getJdbcUrl());
        config.setUsername(postgres.getUsername());
        config.setPassword(postgres.getPassword());
        dataSource = new com.zaxxer.hikari.HikariDataSource(config);

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("""
                CREATE TABLE users (
                    id BIGSERIAL PRIMARY KEY,
                    email VARCHAR(255) UNIQUE NOT NULL,
                    username VARCHAR(100) UNIQUE NOT NULL,
                    password_hash TEXT NOT NULL,
                    role VARCHAR(20) DEFAULT 'USER'
                );
                CREATE TABLE function_types (
                    id BIGSERIAL PRIMARY KEY,
                    name VARCHAR(100) UNIQUE NOT NULL,
                    localized_name VARCHAR(255) NOT NULL,
                    priority INT DEFAULT 0
                );
                CREATE TABLE tabulated_functions (
                    id BIGSERIAL PRIMARY KEY,
                    owner_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                    function_type_id BIGINT NOT NULL REFERENCES function_types(id) ON DELETE CASCADE,
                    serialized_data BYTEA NOT NULL,
                    name VARCHAR(255) NOT NULL,
                    created_at TIMESTAMP DEFAULT NOW(),
                    updated_at TIMESTAMP DEFAULT NOW()
                );
                INSERT INTO function_types (name, localized_name, priority)
                VALUES ('TABULATED', 'Табулированная', 1), ('SIN', 'Синус', 2), ('COS', 'Косинус', 3);
                """);
        }

        userDao = new UserDaoImpl(dataSource);
        typeDao = new FunctionTypeDaoImpl(dataSource);
        dao = new TabulatedFunctionDaoImpl(dataSource);

        logger.info("DAO и БД инициализированы");
    }

    private byte[] data(String s) {
        return s.getBytes(StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("Сортировка по имени (A→Я): findByOwnerIdSortedByNameAsc")
    void sortingByNameAsc_works() {
        logger.info("Тест: сортировка по имени (A→Я)");

        Long userId = userDao.save(new UserDTO("u@u.com", "user", "hash"));
        Long typeId = typeDao.findByName("TABULATED").get().getId();

        // Создаём в "перемешанном" порядке
        dao.save(new TabulatedFunctionDTO(userId, typeId, data("z"), "Zeta"));
        dao.save(new TabulatedFunctionDTO(userId, typeId, data("alpha"), "Alpha"));
        dao.save(new TabulatedFunctionDTO(userId, typeId, data("beta"), "Beta"));

        List<TabulatedFunctionDTO> list = dao.findByOwnerIdSortedByNameAsc(userId);
        List<String> names = list.stream().map(TabulatedFunctionDTO::getName).collect(Collectors.toList());

        assertThat(names).containsExactly("Alpha", "Beta", "Zeta");
        logger.debug("Результат сортировки по имени: {}", names);
        logger.info("Тест пройден");
    }

    @Test
    @DisplayName("Сортировка по дате создания (новые→старые): findByOwnerIdSortedByCreatedAtDesc")
    void sortingByCreatedAtDesc_works() {
        logger.info("🧪 Тест: сортировка по дате (новые → старые)");

        Long userId = userDao.save(new UserDTO("u2@u.com", "u2", "h"));
        Long typeId = typeDao.findByName("SIN").get().getId();

        // Сохраняем в порядке: сначала старая, потом новая
        Long old = dao.save(
                new TabulatedFunctionDTO(userId, typeId, data("old"), "Old"));
        sleep(100); // даём 100 мс, чтобы created_at точно различался
        Long recent = dao.save(
                new TabulatedFunctionDTO(userId, typeId, data("new"), "Recent"));

        List<TabulatedFunctionDTO> list = dao.findByOwnerIdSortedByCreatedAtDesc(userId);
        List<String> names = list.stream().map(TabulatedFunctionDTO::getName).collect(Collectors.toList());

        assertThat(names).containsExactly("Recent", "Old");
        assertThat(list.get(0).getCreatedAt()).isAfter(list.get(1).getCreatedAt());
        logger.debug("Результат: новые выше — {}", names);
        logger.info("Тест пройден");
    }

    @Test
    @DisplayName("Фильтрация + сортировка: findByOwnerIdAndFunctionTypeIdSortedByNameAsc")
    void filteringAndSorting_works() {
        logger.info("Тест: фильтрация по типу + сортировка по имени");

        Long userId = userDao.save(new UserDTO("u3@u.com", "u3", "h"));
        Long tabId = typeDao.findByName("TABULATED").get().getId();
        Long sinId = typeDao.findByName("SIN").get().getId();

        // Создаём смешанные данные
        dao.save(new TabulatedFunctionDTO(userId, sinId, data("z"), "SIN-Z"));
        dao.save(new TabulatedFunctionDTO(userId, tabId, data("a"), "TAB-A"));
        dao.save(new TabulatedFunctionDTO(userId, sinId, data("a"), "SIN-A"));

        List<TabulatedFunctionDTO> sinFunctions = dao.findByOwnerIdAndTypeIdSortedByNameAsc(userId, sinId);
        List<String> names = sinFunctions.stream().map(TabulatedFunctionDTO::getName).collect(Collectors.toList());

        assertThat(sinFunctions).hasSize(2);
        assertThat(names).containsExactly("SIN-A", "SIN-Z");
        logger.debug("Только SIN-функции, отсортированы по имени: {}", names);
        logger.info("Тест пройден");
    }

    @Test
    @DisplayName("Изоляция сохраняется при сортировке")
    void isolation_preserved_in_sorted_search() {
        logger.info("Тест: изоляция + сортировка");

        Long u1 = userDao.save(new UserDTO("a@a.com", "a", "h"));
        Long u2 = userDao.save(new UserDTO("b@b.com", "b", "h"));
        Long typeId = typeDao.findByName("COS").get().getId();

        dao.save(new TabulatedFunctionDTO(u1, typeId, data("u1"), "Func-U1"));
        dao.save(new TabulatedFunctionDTO(u2, typeId, data("u2"), "Alpha-U2")); // раньше в алфавите

        List<TabulatedFunctionDTO> u1List = dao.findByOwnerIdSortedByNameAsc(u1);
        assertThat(u1List).hasSize(1);
        assertThat(u1List.get(0).getName()).isEqualTo("Func-U1");

        List<TabulatedFunctionDTO> u2List = dao.findByOwnerIdSortedByNameAsc(u2);
        assertThat(u2List).hasSize(1);
        assertThat(u2List.get(0).getName()).isEqualTo("Alpha-U2");

        logger.debug("Пользователи видят только свои функции, даже при сортировке");
        logger.info("Тест пройден");
    }

    // Вспомогательный метод для тестов с created_at
    private TabulatedFunctionDTO saveAndReturn(TabulatedFunctionDTO dto) {
        Long id = dao.save(dto);
        return dao.findByIdAndOwnerId(id, dto.getOwnerId()).get();
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { }
    }
}