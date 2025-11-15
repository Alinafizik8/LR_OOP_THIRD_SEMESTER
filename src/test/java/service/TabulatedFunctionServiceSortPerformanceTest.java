package service;

import entity.FunctionTypeEntity;
import entity.TabulatedFunctionEntity;
import entity.UserEntity;
import repository.FunctionTypeRepository;
import repository.TabulatedFunctionRepository;
import repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = Application.class)
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password="
})
class TabulatedFunctionServiceSortPerformanceTest {

    @Autowired
    private TabulatedFunctionService functionService;

    @Autowired
    private TabulatedFunctionRepository functionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FunctionTypeRepository functionTypeRepository;

    private UserEntity testUser;
    private FunctionTypeEntity testType;
    private static final int NUM_FUNCTIONS = 10_000;

    private void setUpTestData() {
        userRepository.deleteAll();
        functionTypeRepository.deleteAll();

        testUser = new UserEntity();
        testUser.setUsername("perfuser_sort_" + System.currentTimeMillis());
        testUser.setEmail("perf_sort_" + System.currentTimeMillis() + "@example.com");
        testUser.setPasswordHash("hash");
        testUser.setRole("USER");
        testUser = userRepository.save(testUser);

        testType = new FunctionTypeEntity();
        testType.setName("PerfTypeSort_" + System.currentTimeMillis());
        testType.setLocalizedName("Тип Производительности Сортировки");
        testType.setPriority(1);
        testType = functionTypeRepository.save(testType);
    }

    private void generateFunctions(int count) {
        functionRepository.deleteAll();
        byte[] data = new byte[1024];

        for (int i = 0; i < count; i++) {
            TabulatedFunctionEntity func = new TabulatedFunctionEntity();
            func.setOwner(testUser);
            func.setFunctionType(testType);
            func.setName(String.format("Func_%05d_%d", i, (int)(Math.random() * 1000)));
            func.setSerializedData(data);
            functionRepository.save(func);
        }
        org.springframework.util.Assert.isTrue(functionRepository.findByOwnerId(testUser.getId()).size() == count, "Данные не были созданы корректно");
    }

    @Test
    void performanceTest_SortByName() {
        setUpTestData();
        generateFunctions(NUM_FUNCTIONS);

        long startTime = System.nanoTime();
        List<TabulatedFunctionEntity> sortedByName = functionService.findFunctionsByOwnerIdSortedByName(testUser.getId());
        long endTime = System.nanoTime();

        long durationMs = (endTime - startTime) / 1_000_000;
        System.out.println("Сортировка по имени (ASC) для " + NUM_FUNCTIONS + " функций заняла: " + durationMs + " мс");

        assertTrue(sortedByName.size() == NUM_FUNCTIONS);
        if (sortedByName.size() > 1) {
            assertTrue(sortedByName.get(0).getName().compareTo(sortedByName.get(sortedByName.size() - 1).getName()) <= 0);
        }
    }

    @Test
    void performanceTest_SortByCreationDateDesc() {
        setUpTestData();
        functionRepository.deleteAll();
        byte[] data = new byte[1024];

        for (int i = 0; i < NUM_FUNCTIONS; i++) {
            TabulatedFunctionEntity func = new TabulatedFunctionEntity();
            func.setOwner(testUser);
            func.setFunctionType(testType);
            func.setName("Func_" + i);
            func.setSerializedData(data);
            functionRepository.save(func);
        }

        long startTime = System.nanoTime();
        List<TabulatedFunctionEntity> sortedByDate = functionService.findFunctionsByOwnerIdSortedByCreationDate(testUser.getId());
        long endTime = System.nanoTime();

        long durationMs = (endTime - startTime) / 1_000_000;
        System.out.println("Сортировка по дате создания (DESC) для " + NUM_FUNCTIONS + " функций заняла: " + durationMs + " мс");

        assertTrue(sortedByDate.size() == NUM_FUNCTIONS);
        if (sortedByDate.size() > 1) {
            assertTrue(sortedByDate.get(0).getCreatedAt().compareTo(sortedByDate.get(sortedByDate.size() - 1).getCreatedAt()) >= 0);
        }
    }

    @Test
    void performanceTest_PaginationAndSort() {
        setUpTestData();
        generateFunctions(NUM_FUNCTIONS);
        int pageSize = 100;
        int totalPages = (int) Math.ceil((double) NUM_FUNCTIONS / pageSize);

        long startTime = System.nanoTime();
        for (int page = 0; page < totalPages; page++) {
            Page<TabulatedFunctionEntity> pageResult = functionService.findFunctionsByOwnerIdPaged(testUser.getId(), page, pageSize, "name", "asc");
            assertTrue(pageResult.getContent().size() <= pageSize);
        }
        long endTime = System.nanoTime();

        long durationMs = (endTime - startTime) / 1_000_000;
        System.out.println("Пагинация и сортировка (100 элементов за раз) для " + NUM_FUNCTIONS + " функций заняла: " + durationMs + " мс");
    }
}