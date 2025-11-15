package service;

import entity.FunctionTypeEntity;
import entity.TabulatedFunctionEntity;
import entity.UserEntity;
import repository.FunctionTypeRepository;
import repository.TabulatedFunctionRepository;
import repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = Application.class)
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password="
})
class TabulatedFunctionServiceSortTest {

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

    @BeforeEach
    void setUp() {
        functionRepository.deleteAll();
        userRepository.deleteAll();
        functionTypeRepository.deleteAll();

        testUser = new UserEntity();
        testUser.setUsername("testuser_sort");
        testUser.setEmail("test_sort@example.com");
        testUser.setPasswordHash("hash");
        testUser.setRole("USER");
        testUser = userRepository.save(testUser);

        testType = new FunctionTypeEntity();
        testType.setName("TestTypeSort");
        testType.setLocalizedName("Тестовый Тип Сортировки");
        testType.setPriority(1);
        testType = functionTypeRepository.save(testType);
    }

    @Test
    void testFindFunctionsByOwnerIdSortedByName() {
        byte[] data = new byte[]{1, 2, 3};

        TabulatedFunctionEntity func3 = new TabulatedFunctionEntity();
        func3.setOwner(testUser);
        func3.setFunctionType(testType);
        func3.setName("ZFunc");
        func3.setSerializedData(data);
        functionRepository.save(func3);

        TabulatedFunctionEntity func1 = new TabulatedFunctionEntity();
        func1.setOwner(testUser);
        func1.setFunctionType(testType);
        func1.setName("AFunc");
        func1.setSerializedData(data);
        functionRepository.save(func1);

        TabulatedFunctionEntity func2 = new TabulatedFunctionEntity();
        func2.setOwner(testUser);
        func2.setFunctionType(testType);
        func2.setName("MFunc");
        func2.setSerializedData(data);
        functionRepository.save(func2);

        List<TabulatedFunctionEntity> sortedFunctions = functionService.findFunctionsByOwnerIdSortedByName(testUser.getId());

        assertEquals(3, sortedFunctions.size());
        assertEquals("AFunc", sortedFunctions.get(0).getName());
        assertEquals("MFunc", sortedFunctions.get(1).getName());
        assertEquals("ZFunc", sortedFunctions.get(2).getName());
    }

    @Test
    void testFindFunctionsByOwnerIdSortedByCreationDateDesc() {
        byte[] data = new byte[]{1, 2, 3};

        TabulatedFunctionEntity func1 = new TabulatedFunctionEntity();
        func1.setOwner(testUser);
        func1.setFunctionType(testType);
        func1.setName("FirstCreated");
        func1.setSerializedData(data);
        func1 = functionRepository.save(func1);

        try { Thread.sleep(10); } catch (InterruptedException e) {}

        TabulatedFunctionEntity func2 = new TabulatedFunctionEntity();
        func2.setOwner(testUser);
        func2.setFunctionType(testType);
        func2.setName("SecondCreated");
        func2.setSerializedData(data);
        func2 = functionRepository.save(func2);

        try { Thread.sleep(10); } catch (InterruptedException e) {}

        TabulatedFunctionEntity func3 = new TabulatedFunctionEntity();
        func3.setOwner(testUser);
        func3.setFunctionType(testType);
        func3.setName("LastCreated");
        func3.setSerializedData(data);
        func3 = functionRepository.save(func3);

        List<TabulatedFunctionEntity> sortedFunctions = functionService.findFunctionsByOwnerIdSortedByCreationDate(testUser.getId());

        assertEquals(3, sortedFunctions.size());
        assertEquals("LastCreated", sortedFunctions.get(0).getName());
        assertEquals("SecondCreated", sortedFunctions.get(1).getName());
        assertEquals("FirstCreated", sortedFunctions.get(2).getName());
    }
}