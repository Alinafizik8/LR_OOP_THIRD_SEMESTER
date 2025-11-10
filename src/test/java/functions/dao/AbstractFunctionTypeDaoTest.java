package functions.dao;

import functions.dto.FunctionTypeDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

public abstract class AbstractFunctionTypeDaoTest {

    private static final Logger logger = LoggerFactory.getLogger(AbstractFunctionTypeDaoTest.class);
    protected FunctionTypeDao dao;

    protected abstract void setupDatabase();
    protected abstract FunctionTypeDao createDao();

    @BeforeEach
    void setUp() {
        logger.debug("Initializing test context for FunctionTypeDao");
        setupDatabase();
        dao = createDao();
        logger.debug("DAO initialized: {}", dao.getClass().getSimpleName());
    }

    @Test
    void save_and_findById_works() {
        logger.info("Test: save_and_findById_works");

        FunctionTypeDTO type = new FunctionTypeDTO("TEST", "Тест", 99);
        Long id = dao.save(type);
        logger.debug("Saved function type: id={}, name='{}', priority={}", id, type.getName(), type.getPriority());

        Optional<FunctionTypeDTO> found = dao.findById(id);
        assertThat(found).isPresent();
        FunctionTypeDTO f = found.get();
        assertThat(f.getName()).isEqualTo("TEST");
        logger.debug("Found type: id={}, name='{}', localized='{}'", f.getId(), f.getName(), f.getLocalizedName());

        logger.info("Test passed: save_and_findById_works");
    }

    @Test
    void unique_name_enforced() {
        logger.info("Test: unique_name_enforced");

        dao.save(new FunctionTypeDTO("UNIQ", "Уник", 1));
        logger.debug("First type 'UNIQ' saved");

        RuntimeException ex = catchThrowableOfType(() ->
                        dao.save(new FunctionTypeDTO("UNIQ", "Другой", 2)),
                RuntimeException.class
        );
        assertThat(ex).isNotNull();
        logger.warn("Duplicate name 'UNIQ' rejected as expected");

        logger.info("Test passed: unique_name_enforced");
    }

    @Test
    void findByName_works() {
        logger.info("Test: findByName_works");

        dao.save(new FunctionTypeDTO("FIND", "Найти", 1));
        logger.debug("Type 'FIND' created");

        Optional<FunctionTypeDTO> found = dao.findByName("FIND");
        assertThat(found).isPresent();
        logger.debug("Found by name: '{}'", found.get().getName());

        Optional<FunctionTypeDTO> missing = dao.findByName("MISS");
        assertThat(missing).isEmpty();
        logger.debug("Type 'MISS' not found (as expected)");

        logger.info("Test passed: findByName_works");
    }

    @Test
    void findAll_sortedByPriority_works() {
        logger.info("Test: findAll_sortedByPriority_works");

        dao.save(new FunctionTypeDTO("Z", "Zeta", 10));
        dao.save(new FunctionTypeDTO("A", "Alpha", 1));
        dao.save(new FunctionTypeDTO("B", "Beta", 1));
        logger.debug("Created 3 function types");

        List<FunctionTypeDTO> list = dao.findAllSortedByPriority();
        assertThat(list).hasSize(3);
        List<String> names = list.stream().map(FunctionTypeDTO::getName).toList();
        assertThat(names).containsExactly("A", "B", "Z");
        logger.debug("Sorted list: {}", names);

        logger.info("Test passed: findAll_sortedByPriority_works");
    }

    @Test
    void update_works() {
        logger.info("Test: update_works");

        Long id = dao.save(new FunctionTypeDTO("OLD", "Старое", 5));
        logger.debug("Type id={} saved with name='OLD', priority=5", id);

        FunctionTypeDTO updated = new FunctionTypeDTO("NEW", "Новое", 6);
        updated.setId(id);
        dao.update(updated);
        logger.debug("Updated type id={} → name='{}', priority={}", id, updated.getName(), updated.getPriority());

        FunctionTypeDTO f = dao.findById(id).get();
        assertThat(f.getName()).isEqualTo("NEW");
        assertThat(f.getPriority()).isEqualTo(6);
        logger.debug("Verified: name='{}', priority={}", f.getName(), f.getPriority());

        logger.info("Test passed: update_works");
    }

    @Test
    void delete_works() {
        logger.info("Test: delete_works");

        Long id = dao.save(new FunctionTypeDTO("DEL", "Удалить", 0));
        logger.debug("Type id={} created for deletion", id);

        dao.deleteById(id);
        logger.debug("🗑Deleted type id={}", id);

        Optional<FunctionTypeDTO> found = dao.findById(id);
        assertThat(found).isEmpty();
        logger.debug("Confirmed: type id={} no longer exists", id);

        logger.info("Test passed: delete_works");
    }
}