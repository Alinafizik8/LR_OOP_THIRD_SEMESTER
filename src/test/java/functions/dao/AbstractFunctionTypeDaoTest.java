package functions.dao;

import functions.dto.FunctionTypeDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

public abstract class AbstractFunctionTypeDaoTest {

    protected FunctionTypeDao dao;

    protected abstract void setupDatabase();
    protected abstract FunctionTypeDao createDao();

    @BeforeEach
    void setUp() {
        setupDatabase();
        dao = createDao();
    }

    @Test
    void save_and_findById_works() {
        FunctionTypeDTO type = new FunctionTypeDTO("TEST", "Тест", 99);
        Long id = dao.save(type);

        Optional<FunctionTypeDTO> found = dao.findById(id);
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("TEST");
    }

    @Test
    void unique_name_enforced() {
        dao.save(new FunctionTypeDTO("UNIQ", "Уник", 1));
        assertThatThrownBy(() ->
                dao.save(new FunctionTypeDTO("UNIQ", "Другой", 2))
        ).isInstanceOf(RuntimeException.class);
    }

    @Test
    void findByName_works() {
        dao.save(new FunctionTypeDTO("FIND", "Найти", 1));
        assertThat(dao.findByName("FIND")).isPresent();
        assertThat(dao.findByName("MISS")).isEmpty();
    }

    @Test
    void findAll_sortedByPriority_works() {
        dao.save(new FunctionTypeDTO("Z", "Zeta", 10));
        dao.save(new FunctionTypeDTO("A", "Alpha", 1));
        dao.save(new FunctionTypeDTO("B", "Beta", 1));

        List<FunctionTypeDTO> list = dao.findAllSortedByPriority();
        assertThat(list).extracting(FunctionTypeDTO::getName)
                .containsExactly("A", "B", "Z");
    }

    @Test
    void update_works() {
        Long id = dao.save(new FunctionTypeDTO("OLD", "Старое", 5));
        FunctionTypeDTO updated = new FunctionTypeDTO("NEW", "Новое", 6);
        updated.setId(id);
        dao.update(updated);

        FunctionTypeDTO f = dao.findById(id).get();
        assertThat(f.getName()).isEqualTo("NEW");
        assertThat(f.getPriority()).isEqualTo(6);
    }

    @Test
    void delete_works() {
        Long id = dao.save(new FunctionTypeDTO("DEL", "Удалить", 0));
        dao.deleteById(id);
        assertThat(dao.findById(id)).isEmpty();
    }
}