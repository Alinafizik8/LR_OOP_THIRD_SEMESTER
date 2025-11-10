package dto;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class FunctionTypeDTOTest {

    @Test
    void testConstructorAndGetters() {
        Long id = 1L;
        String name = "array_function";
        String localizedName = "Array Function";
        Integer priority = 10;
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2024, 1, 2, 15, 30);

        FunctionTypeDTO dto = new FunctionTypeDTO(id, name, localizedName, priority, createdAt, updatedAt);

        assertEquals(id, dto.getId());
        assertEquals(name, dto.getName());
        assertEquals(localizedName, dto.getLocalizedName());
        assertEquals(priority, dto.getPriority());
        assertEquals(createdAt, dto.getCreatedAt());
        assertEquals(updatedAt, dto.getUpdatedAt());
    }

    @Test
    void testSetters() {
        FunctionTypeDTO dto = new FunctionTypeDTO(1L, "old_name", "Old Name", 5,
                LocalDateTime.now(), LocalDateTime.now());

        dto.setId(2L);
        dto.setName("new_name");
        dto.setLocalizedName("New Name");
        dto.setPriority(15);
        LocalDateTime newCreatedAt = LocalDateTime.of(2024, 2, 1, 12, 0);
        LocalDateTime newUpdatedAt = LocalDateTime.of(2024, 2, 2, 18, 0);
        dto.setCreatedAt(newCreatedAt);
        dto.setUpdatedAt(newUpdatedAt);

        assertEquals(2L, dto.getId());
        assertEquals("new_name", dto.getName());
        assertEquals("New Name", dto.getLocalizedName());
        assertEquals(15, dto.getPriority());
        assertEquals(newCreatedAt, dto.getCreatedAt());
        assertEquals(newUpdatedAt, dto.getUpdatedAt());
    }

    @Test
    void testSettersWithNullValues() {
        FunctionTypeDTO dto = new FunctionTypeDTO(1L, "name", "Localized", 1,
                LocalDateTime.now(), LocalDateTime.now());

        dto.setId(null);
        dto.setName(null);
        dto.setLocalizedName(null);
        dto.setPriority(null);
        dto.setCreatedAt(null);
        dto.setUpdatedAt(null);

        assertNull(dto.getId());
        assertNull(dto.getName());
        assertNull(dto.getLocalizedName());
        assertNull(dto.getPriority());
        assertNull(dto.getCreatedAt());
        assertNull(dto.getUpdatedAt());
    }

    @Test
    void testConstructorWithNullValues() {
        FunctionTypeDTO dto = new FunctionTypeDTO(null, null, null, null, null, null);

        assertNull(dto.getId());
        assertNull(dto.getName());
        assertNull(dto.getLocalizedName());
        assertNull(dto.getPriority());
        assertNull(dto.getCreatedAt());
        assertNull(dto.getUpdatedAt());
    }

    @Test
    void testToString() {
        Long id = 123L;
        String name = "linked_list";
        String localizedName = "Linked List Function";
        Integer priority = 20;
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 15, 9, 30);
        LocalDateTime updatedAt = LocalDateTime.of(2024, 1, 20, 14, 45);

        FunctionTypeDTO dto = new FunctionTypeDTO(id, name, localizedName, priority, createdAt, updatedAt);

        String result = dto.toString();

        assertNotNull(result);
        assertTrue(result.contains("FunctionTypeDTO{"));
        assertTrue(result.contains("id=" + id));
        assertTrue(result.contains("name='" + name + "'"));
        assertTrue(result.contains("localizedName='" + localizedName + "'"));
        assertTrue(result.contains("priority=" + priority));
        assertTrue(result.contains("createdAt=" + createdAt));
        assertTrue(result.contains("updatedAt=" + updatedAt));
    }

    @Test
    void testToStringWithNullValues() {
        FunctionTypeDTO dto = new FunctionTypeDTO(null, null, null, null, null, null);

        String result = dto.toString();

        assertNotNull(result);
        assertTrue(result.contains("FunctionTypeDTO{"));
        assertTrue(result.contains("id=null"));
        assertTrue(result.contains("name='null'"));
        assertTrue(result.contains("localizedName='null'"));
        assertTrue(result.contains("priority=null"));
        assertTrue(result.contains("createdAt=null"));
        assertTrue(result.contains("updatedAt=null"));
    }

    @Test
    void testMultipleSetterCalls() {
        FunctionTypeDTO dto = new FunctionTypeDTO(1L, "initial", "Initial", 1,
                LocalDateTime.now(), LocalDateTime.now());

        dto.setId(10L);
        dto.setId(20L);
        dto.setName("first");
        dto.setName("second");
        dto.setLocalizedName("First");
        dto.setLocalizedName("Second");
        dto.setPriority(5);
        dto.setPriority(10);

        LocalDateTime time1 = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime time2 = LocalDateTime.of(2024, 1, 2, 0, 0);
        dto.setCreatedAt(time1);
        dto.setCreatedAt(time2);
        dto.setUpdatedAt(time1);
        dto.setUpdatedAt(time2);

        assertEquals(20L, dto.getId());
        assertEquals("second", dto.getName());
        assertEquals("Second", dto.getLocalizedName());
        assertEquals(10, dto.getPriority());
        assertEquals(time2, dto.getCreatedAt());
        assertEquals(time2, dto.getUpdatedAt());
    }

    @Test
    void testEmptyAndSpecialCharactersInNames() {
        FunctionTypeDTO dto = new FunctionTypeDTO(1L, "original", "Original", 1,
                LocalDateTime.now(), LocalDateTime.now());

        dto.setName("");
        dto.setLocalizedName("");
        assertEquals("", dto.getName());
        assertEquals("", dto.getLocalizedName());

        dto.setName("function@123#test$");
        dto.setLocalizedName("Функция №1");
        assertEquals("function@123#test$", dto.getName());
        assertEquals("Функция №1", dto.getLocalizedName());

        dto.setName("my_test_function");
        dto.setLocalizedName("My Test Function");
        assertEquals("my_test_function", dto.getName());
        assertEquals("My Test Function", dto.getLocalizedName());
    }

    @Test
    void testPriorityBoundaries() {
        FunctionTypeDTO dto = new FunctionTypeDTO(1L, "test", "Test", 0,
                LocalDateTime.now(), LocalDateTime.now());

        dto.setPriority(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, dto.getPriority());

        dto.setPriority(Integer.MIN_VALUE);
        assertEquals(Integer.MIN_VALUE, dto.getPriority());

        dto.setPriority(0);
        assertEquals(0, dto.getPriority());

        dto.setPriority(-100);
        assertEquals(-100, dto.getPriority());

        dto.setPriority(1000);
        assertEquals(1000, dto.getPriority());
    }

    @Test
    void testDateTimeBoundaries() {
        LocalDateTime minDateTime = LocalDateTime.MIN;
        LocalDateTime maxDateTime = LocalDateTime.MAX;

        FunctionTypeDTO dto = new FunctionTypeDTO(1L, "test", "Test", 1,
                minDateTime, maxDateTime);

        assertEquals(minDateTime, dto.getCreatedAt());
        assertEquals(maxDateTime, dto.getUpdatedAt());

        dto.setCreatedAt(maxDateTime);
        dto.setUpdatedAt(minDateTime);

        assertEquals(maxDateTime, dto.getCreatedAt());
        assertEquals(minDateTime, dto.getUpdatedAt());
    }

    @Test
    void testNullPriority() {
        FunctionTypeDTO dto = new FunctionTypeDTO(1L, "test", "Test", null,
                LocalDateTime.now(), LocalDateTime.now());

        assertNull(dto.getPriority());

        dto.setPriority(5);
        assertEquals(5, dto.getPriority());

        dto.setPriority(null);
        assertNull(dto.getPriority());
    }

    @Test
    void testLongIdValues() {
        FunctionTypeDTO dto = new FunctionTypeDTO(Long.MAX_VALUE, "test", "Test", 1,
                LocalDateTime.now(), LocalDateTime.now());

        assertEquals(Long.MAX_VALUE, dto.getId());

        dto.setId(Long.MIN_VALUE);
        assertEquals(Long.MIN_VALUE, dto.getId());

        dto.setId(0L);
        assertEquals(0L, dto.getId());
    }
}