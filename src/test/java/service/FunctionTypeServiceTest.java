package service;

import Entity.FunctionTypeEntity;
import Repository.FunctionTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = Application.class)
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password="
})
class FunctionTypeServiceSortTest {

    @Autowired
    private FunctionTypeService typeService;

    @Autowired
    private FunctionTypeRepository typeRepository;

    @BeforeEach
    void setUp() {
        typeRepository.deleteAll();
    }

    @Test
    void testFindAllTypesSortedByPriority() {
        FunctionTypeEntity type3 = new FunctionTypeEntity();
        type3.setName("TypeC");
        type3.setLocalizedName("Тип В");
        type3.setPriority(2);
        typeRepository.save(type3);

        FunctionTypeEntity type1 = new FunctionTypeEntity();
        type1.setName("TypeA");
        type1.setLocalizedName("Тип А");
        type1.setPriority(1);
        typeRepository.save(type1);

        FunctionTypeEntity type2 = new FunctionTypeEntity();
        type2.setName("TypeB");
        type2.setLocalizedName("Тип Г");
        type2.setPriority(1);
        typeRepository.save(type2);

        FunctionTypeEntity type4 = new FunctionTypeEntity();
        type4.setName("TypeD");
        type4.setLocalizedName("Тип Д");
        type4.setPriority(3);
        typeRepository.save(type4);

        List<FunctionTypeEntity> sortedTypes = typeService.findAllTypesSortedByPriority();

        assertEquals(4, sortedTypes.size());

        assertTrue(sortedTypes.get(0).getPriority() == 1 || sortedTypes.get(1).getPriority() == 1);
        assertTrue(sortedTypes.get(0).getPriority() == 1 || sortedTypes.get(1).getPriority() == 1);

        assertEquals(2, sortedTypes.get(2).getPriority());
        assertEquals("Тип В", sortedTypes.get(2).getLocalizedName());

        assertEquals(3, sortedTypes.get(3).getPriority());
        assertEquals("Тип Д", sortedTypes.get(3).getLocalizedName());

        List<String> localizedNames = sortedTypes.stream()
                .map(FunctionTypeEntity::getLocalizedName)
                .collect(Collectors.toList());
        assertTrue(localizedNames.contains("Тип А"));
        assertTrue(localizedNames.contains("Тип Г"));
        assertTrue(localizedNames.contains("Тип В"));
        assertTrue(localizedNames.contains("Тип Д"));
    }
}