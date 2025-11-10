package functions.dao;

import functions.dto.TabulatedFunctionDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.assertj.core.api.Assertions.*;

/**
 * Абстрактный тест-контракт для интерфейса TabulatedFunctionDao.
 * Наследники обязаны предоставить:
 * - DataSource (или другой способ инициализации),
 * - реализацию интерфейса,
 * - вспомогательные методы для создания зависимостей (user, type).
 */
public abstract class AbstractTabulatedFunctionDaoTest {

    private static final Logger logger = LoggerFactory.getLogger(AbstractTabulatedFunctionDaoTest.class);
    protected TabulatedFunctionDao dao;
    protected Random random = new Random();

    // Наследники должны реализовать
    protected abstract void setupDatabase();
    protected abstract Long createTestUser();
    protected abstract Long getTabulatedFunctionTypeId();

    @BeforeEach
    void setUp() {
        logger.debug("Initializing test context for TabulatedFunctionDao");
        setupDatabase();
        dao = createDao();
        logger.debug("DAO initialized: {}", dao.getClass().getSimpleName());
    }

    protected abstract TabulatedFunctionDao createDao();

    private byte[] randomBytes(int n) {
        byte[] b = new byte[n];
        random.nextBytes(b);
        if (n > 0) {
            logger.trace("Generated {} random bytes (sample: {})", n,
                    java.util.Arrays.toString(java.util.Arrays.copyOf(b, Math.min(3, n))));
        }
        return b;
    }

    @Test
    void save_and_findByIdAndOwnerId_works() {
        logger.info("Test: save_and_findByIdAndOwnerId_works");

        Long userId = createTestUser();
        Long typeId = getTabulatedFunctionTypeId();
        logger.debug("Created dependencies: user_id={}, type_id={}", userId, typeId);

        TabulatedFunctionDTO dto = new TabulatedFunctionDTO();
        dto.setOwnerId(userId);
        dto.setFunctionTypeId(typeId);
        dto.setName("test_func");
        dto.setSerializedData(randomBytes(100));

        Long id = dao.save(dto);
        logger.debug("Saved function: id={}, name='{}', size={}B", id, dto.getName(), dto.getSerializedData().length);

        Optional<TabulatedFunctionDTO> found = dao.findByIdAndOwnerId(id, userId);
        assertThat(found).isPresent();
        TabulatedFunctionDTO f = found.get();
        assertThat(f.getName()).isEqualTo("test_func");
        assertThat(f.getSerializedData()).hasSize(100);

        logger.info("Test passed: save_and_findByIdAndOwnerId_works");
    }

    @Test
    void isolation_prevents_access_to_other_users_data() {
        logger.info("🧪 Test: isolation_prevents_access_to_other_users_data");

        Long u1 = createTestUser();
        Long u2 = createTestUser();
        Long typeId = getTabulatedFunctionTypeId();
        logger.debug("Created users: u1={}, u2={}; type_id={}", u1, u2, typeId);

        TabulatedFunctionDTO dto = new TabulatedFunctionDTO(u1, typeId, randomBytes(1), "private");
        Long id = dao.save(dto);
        logger.debug("Function id={} belongs to user {}", id, u1);

        Optional<TabulatedFunctionDTO> own = dao.findByIdAndOwnerId(id, u1);
        assertThat(own).isPresent();
        logger.debug("Owner u1 accessed function id={}", id);

        Optional<TabulatedFunctionDTO> other = dao.findByIdAndOwnerId(id, u2);
        assertThat(other).isEmpty();
        logger.debug("User u2 failed to access function id={}", id);

        logger.info("Test passed: isolation_prevents_access_to_other_users_data");
    }

    @Test
    void updateName_works() {
        logger.info("Test: updateName_works");

        Long userId = createTestUser();
        Long typeId = getTabulatedFunctionTypeId();
        Long id = dao.save(new TabulatedFunctionDTO(userId, typeId, randomBytes(1), "old"));
        logger.debug("Created function id={} with name='old'", id);

        dao.updateName(id, userId, "new_name");
        logger.debug("Updated name to 'new_name'");

        String actual = dao.findByIdAndOwnerId(id, userId).get().getName();
        assertThat(actual).isEqualTo("new_name");
        logger.debug("Verified new name: '{}'", actual);

        logger.info("Test passed: updateName_works");
    }

    @Test
    void updateDataAndName_works() {
        logger.info("Test: updateDataAndName_works");

        Long userId = createTestUser();
        Long typeId = getTabulatedFunctionTypeId();
        Long id = dao.save(new TabulatedFunctionDTO(userId, typeId, new byte[]{1}, "old"));
        logger.debug("Function id={} created (data size: 1B)", id);

        TabulatedFunctionDTO updated = new TabulatedFunctionDTO();
        updated.setSerializedData(new byte[]{2, 3});
        updated.setName("updated");
        dao.updateDataAndName(id, userId, updated);
        logger.debug("Updated: name='{}', data size={}B", updated.getName(), updated.getSerializedData().length);

        TabulatedFunctionDTO f = dao.findByIdAndOwnerId(id, userId).get();
        assertThat(f.getName()).isEqualTo("updated");
        assertThat(f.getSerializedData()).containsExactly(2, 3);
        logger.debug("Verified: name='{}', data={}", f.getName(), java.util.Arrays.toString(f.getSerializedData()));

        logger.info("Test passed: updateDataAndName_works");
    }

    @Test
    void deleteByIdAndOwnerId_works() {
        logger.info("Test: deleteByIdAndOwnerId_works");

        Long userId = createTestUser();
        Long typeId = getTabulatedFunctionTypeId();
        Long id = dao.save(new TabulatedFunctionDTO(userId, typeId, randomBytes(1), "to_del"));
        logger.debug("Function id={} marked for deletion", id);

        dao.deleteByIdAndOwnerId(id, userId);
        logger.debug("🗑Deleted function id={} for user {}", id, userId);

        Optional<TabulatedFunctionDTO> found = dao.findByIdAndOwnerId(id, userId);
        assertThat(found).isEmpty();
        logger.debug("Confirmed: function id={} no longer exists", id);

        logger.info("Test passed: deleteByIdAndOwnerId_works");
    }

    @Test
    void findByOwnerId_returns_own_functions() {
        logger.info("Test: findByOwnerId_returns_own_functions");

        Long userId = createTestUser();
        Long typeId = getTabulatedFunctionTypeId();
        logger.debug("Created test user id={}", userId);

        dao.save(new TabulatedFunctionDTO(userId, typeId, randomBytes(1), "f1"));
        dao.save(new TabulatedFunctionDTO(userId, typeId, randomBytes(1), "f2"));
        logger.debug("Saved 2 functions for user {}", userId);

        List<TabulatedFunctionDTO> list = dao.findByOwnerId(userId);
        assertThat(list).hasSize(2);
        List<String> names = list.stream().map(TabulatedFunctionDTO::getName).toList();
        assertThat(names).contains("f1", "f2");
        logger.debug("Loaded {} functions: {}", list.size(), names);

        logger.info("Test passed: findByOwnerId_returns_own_functions");
    }

    @Test
    void findByOwnerIdAndTypeId_filters_correctly() {
        logger.info("Test: findByOwnerIdAndTypeId_filters_correctly");

        Long userId = createTestUser();
        Long tabId = getTabulatedFunctionTypeId();
        Long sinId = createFunctionType("SIN", "Синус", 2);
        logger.debug("Types: TABULATED={}, SIN={}", tabId, sinId);

        dao.save(new TabulatedFunctionDTO(userId, tabId, randomBytes(1), "tab"));
        dao.save(new TabulatedFunctionDTO(userId, sinId, randomBytes(1), "sin"));
        logger.debug("Created 1 TABULATED + 1 SIN function");

        List<TabulatedFunctionDTO> tabs = dao.findByOwnerId(userId);
        assertThat(tabs).hasSize(1);
        assertThat(tabs.get(0).getName()).isEqualTo("tab");
        logger.debug("Found {} TABULATED function(s)", tabs.size());

        logger.info("Test passed: findByOwnerIdAndTypeId_filters_correctly");
    }

    protected Long createFunctionType(String name, String localizedName, int priority) {
        throw new UnsupportedOperationException("Наследник должен реализовать createFunctionType");
    }
}