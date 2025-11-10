package Repository;

import Entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.assertThrows;

@DataJpaTest
class FunctionTypeRepositoryTest {
    private static final Logger logger = LoggerFactory.getLogger(FunctionTypeRepositoryTest.class);
    private static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:15");

    static {
        postgres.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired private FunctionTypeRepository typeRepo;

    @BeforeEach
    void setUp() {
        logger.info("Initializing FunctionTypeRepository test context");
        typeRepo.deleteAll();
        logger.debug("Cleared all function types");
    }

    @Test
    void save_and_findById_works() {
        logger.info("Test: save_and_findById_works");
        FunctionTypeEntity type = new FunctionTypeEntity("TEST", "Тест", 99);
        type = typeRepo.save(type);
        logger.debug("Saved type: id={}, name='{}'", type.getId(), type.getName());

        Optional<FunctionTypeEntity> found = typeRepo.findById(type.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("TEST");
        logger.info("Test passed");
    }

    @Test
    void unique_name_enforced() {
        logger.info("Test: unique_name_enforced");
        typeRepo.save(new FunctionTypeEntity("UNIQ", "Уник", 1));
        logger.debug("First type 'UNIQ' saved");

        assertThrows(org.springframework.dao.DataIntegrityViolationException.class, () ->
                typeRepo.save(new FunctionTypeEntity("UNIQ", "Другой", 2))
        );
        logger.warn("Duplicate name rejected by DB constraint");
        logger.info("Test passed");
    }

    @Test
    void findByName_works() {
        logger.info("Test: findByName_works");
        typeRepo.save(new FunctionTypeEntity("FIND", "Найти", 1));
        logger.debug("Type 'FIND' created");

        assertThat(typeRepo.findByName("FIND")).isPresent();
        assertThat(typeRepo.findByName("MISS")).isEmpty();
        logger.debug("findByName works");
        logger.info("Test passed");
    }

    @Test
    void findAllSortedByPriority_works() {
        logger.info("Test: findAllSortedByPriority_works");
        typeRepo.save(new FunctionTypeEntity("Z", "Zeta", 10));
        typeRepo.save(new FunctionTypeEntity("A", "Alpha", 1));
        typeRepo.save(new FunctionTypeEntity("B", "Beta", 1));
        logger.debug("Created 3 types");

        List<FunctionTypeEntity> list = typeRepo.findAllSortedByPriority();
        assertThat(list).extracting(FunctionTypeEntity::getName)
                .containsExactly("A", "B", "Z");
        logger.debug("Sorted list: {}", list.stream().map(FunctionTypeEntity::getName).toList());
        logger.info("Test passed");
    }

    @Test
    void update_works_via_save() {
        logger.info("Test: update_works_via_save");
        FunctionTypeEntity type = typeRepo.save(new FunctionTypeEntity("OLD", "Старое", 5));
        type.setName("NEW");
        type.setLocalizedName("Новое");
        type.setPriority(6);
        typeRepo.save(type);
        logger.debug("Updated type id={}", type.getId());

        FunctionTypeEntity updated = typeRepo.findById(type.getId()).get();
        assertThat(updated.getName()).isEqualTo("NEW");
        assertThat(updated.getPriority()).isEqualTo(6);
        logger.info("Test passed");
    }

    @Test
    void delete_works() {
        logger.info("Test: delete_works");
        FunctionTypeEntity type = typeRepo.save(new FunctionTypeEntity("DEL", "Удалить", 0));
        typeRepo.deleteById(type.getId());
        logger.debug("Deleted type id={}", type.getId());

        assertThat(typeRepo.findById(type.getId())).isEmpty();
        logger.info("Test passed");
    }
}