package functions.dao;

import functions.dto.TabulatedFunctionDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

    protected TabulatedFunctionDao dao;
    protected Random random = new Random();

    // Наследники должны реализовать
    protected abstract void setupDatabase(); // создаёт таблицы + заполняет function_types
    protected abstract Long createTestUser(); // возвращает ID пользователя
    protected abstract Long getTabulatedFunctionTypeId(); // возвращает ID типа "TABULATED"

    @BeforeEach
    void setUp() {
        setupDatabase();
        dao = createDao(); // реализация от наследника
    }

    protected abstract TabulatedFunctionDao createDao();

    private byte[] randomBytes(int n) {
        byte[] b = new byte[n];
        random.nextBytes(b);
        return b;
    }

    @Test
    void save_and_findByIdAndOwnerId_works() {
        Long userId = createTestUser();
        Long typeId = getTabulatedFunctionTypeId();

        TabulatedFunctionDTO dto = new TabulatedFunctionDTO();
        dto.setOwnerId(userId);
        dto.setFunctionTypeId(typeId);
        dto.setName("test_func");
        dto.setSerializedData(randomBytes(100));

        Long id = dao.save(dto);
        Optional<TabulatedFunctionDTO> found = dao.findByIdAndOwnerId(id, userId);

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("test_func");
        assertThat(found.get().getSerializedData()).hasSize(100);
    }

    @Test
    void isolation_prevents_access_to_other_users_data() {
        Long u1 = createTestUser();
        Long u2 = createTestUser();
        Long typeId = getTabulatedFunctionTypeId();

        TabulatedFunctionDTO dto = new TabulatedFunctionDTO(u1, typeId, randomBytes(1), "private");
        Long id = dao.save(dto);

        assertThat(dao.findByIdAndOwnerId(id, u1)).isPresent();
        assertThat(dao.findByIdAndOwnerId(id, u2)).isEmpty(); // 🔒 изоляция
    }

    @Test
    void updateName_works() {
        Long userId = createTestUser();
        Long typeId = getTabulatedFunctionTypeId();
        Long id = dao.save(new TabulatedFunctionDTO(userId, typeId, randomBytes(1), "old"));

        dao.updateName(id, userId, "new_name");
        assertThat(dao.findByIdAndOwnerId(id, userId).get().getName()).isEqualTo("new_name");
    }

    @Test
    void updateDataAndName_works() {
        Long userId = createTestUser();
        Long typeId = getTabulatedFunctionTypeId();
        Long id = dao.save(new TabulatedFunctionDTO(userId, typeId, new byte[]{1}, "old"));

        TabulatedFunctionDTO updated = new TabulatedFunctionDTO();
        updated.setSerializedData(new byte[]{2, 3});
        updated.setName("updated");
        dao.updateDataAndName(id, userId, updated);

        TabulatedFunctionDTO f = dao.findByIdAndOwnerId(id, userId).get();
        assertThat(f.getName()).isEqualTo("updated");
        assertThat(f.getSerializedData()).containsExactly(2, 3);
    }

    @Test
    void deleteByIdAndOwnerId_works() {
        Long userId = createTestUser();
        Long typeId = getTabulatedFunctionTypeId();
        Long id = dao.save(new TabulatedFunctionDTO(userId, typeId, randomBytes(1), "to_del"));

        dao.deleteByIdAndOwnerId(id, userId);
        assertThat(dao.findByIdAndOwnerId(id, userId)).isEmpty();
    }

    @Test
    void findByOwnerId_returns_own_functions() {
        Long userId = createTestUser();
        Long typeId = getTabulatedFunctionTypeId();

        dao.save(new TabulatedFunctionDTO(userId, typeId, randomBytes(1), "f1"));
        dao.save(new TabulatedFunctionDTO(userId, typeId, randomBytes(1), "f2"));

        List<TabulatedFunctionDTO> list = dao.findByOwnerId(userId);
        assertThat(list).hasSize(2);
        assertThat(list).extracting(TabulatedFunctionDTO::getName).contains("f1", "f2");
    }

    @Test
    void findByOwnerIdAndTypeId_filters_correctly() {
        Long userId = createTestUser();
        Long tabId = getTabulatedFunctionTypeId();
        Long sinId = createFunctionType("SIN", "Синус", 2);

        dao.save(new TabulatedFunctionDTO(userId, tabId, randomBytes(1), "tab"));
        dao.save(new TabulatedFunctionDTO(userId, sinId, randomBytes(1), "sin"));

        List<TabulatedFunctionDTO> tabs = dao.findByOwnerId(userId);
        assertThat(tabs).hasSize(1);
        assertThat(tabs.get(0).getName()).isEqualTo("tab");
    }

    // Вспомогательный метод — наследники могут переопределить
    protected Long createFunctionType(String name, String localizedName, int priority) {
        throw new UnsupportedOperationException("Наследник должен реализовать createFunctionType");
    }
}