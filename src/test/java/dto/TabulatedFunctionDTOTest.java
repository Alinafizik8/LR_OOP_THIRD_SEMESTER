package dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class TabulatedFunctionDTOTest {

    private byte[] sampleData;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        // Простые тестовые данные — не сериализованный объект, а "сырой" массив байтов
        sampleData = new byte[]{0x01, 0x02, 0x03, 0x04, (byte) 0xFF};
        now = LocalDateTime.of(2025, 11, 11, 15, 30, 45);
    }

    // ========= Конструкторы =========
    @Test
    void fullConstructor_works() {
        TabulatedFunctionDTO dto = new TabulatedFunctionDTO(
                10L, 100L, 2L, sampleData, "test_func", now, now.plusSeconds(5)
        );

        assertThat(dto.getId()).isEqualTo(10L);
        assertThat(dto.getOwnerId()).isEqualTo(100L);
        assertThat(dto.getFunctionTypeId()).isEqualTo(2L);
        assertThat(dto.getSerializedData()).isEqualTo(sampleData);
        assertThat(dto.getName()).isEqualTo("test_func");
        assertThat(dto.getCreatedAt()).isEqualTo(now);
        assertThat(dto.getUpdatedAt()).isEqualTo(now.plusSeconds(5));
    }

    @Test
    void minimalConstructor_works() {
        TabulatedFunctionDTO dto = new TabulatedFunctionDTO(5L, 6L, sampleData, "minimal");

        assertThat(dto.getId()).isNull();
        assertThat(dto.getOwnerId()).isEqualTo(5L);
        assertThat(dto.getFunctionTypeId()).isEqualTo(6L);
        assertThat(dto.getSerializedData()).isEqualTo(sampleData);
        assertThat(dto.getName()).isEqualTo("minimal");
        assertThat(dto.getCreatedAt()).isNull();
        assertThat(dto.getUpdatedAt()).isNull();
    }

    @Test
    void defaultConstructor_works() {
        TabulatedFunctionDTO dto = new TabulatedFunctionDTO();
        assertThat(dto).isNotNull();
    }

    // ========= Геттеры и сеттеры =========
    @Test
    void gettersAndSetters_work() {
        TabulatedFunctionDTO dto = new TabulatedFunctionDTO();

        dto.setId(1L);
        dto.setOwnerId(2L);
        dto.setFunctionTypeId(3L);
        dto.setSerializedData(new byte[]{10, 20});
        dto.setName("setter");
        dto.setCreatedAt(LocalDateTime.now());
        dto.setUpdatedAt(LocalDateTime.now().plusHours(1));

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getOwnerId()).isEqualTo(2L);
        assertThat(dto.getFunctionTypeId()).isEqualTo(3L);
        assertThat(dto.getSerializedData()).containsExactly((byte) 10, (byte) 20);
        assertThat(dto.getName()).isEqualTo("setter");
    }

    @Test
    void getSerializedData_returns_copy() {
        TabulatedFunctionDTO dto = new TabulatedFunctionDTO(1L, 1L, sampleData, "copy");
        byte[] original = dto.getSerializedData();
        original[0] = (byte) 0xAA; // меняем копию

        byte[] second = dto.getSerializedData();
        assertThat(second[0]).isNotEqualTo((byte) 0xAA); // оригинал не изменился
        assertThat(second).isEqualTo(sampleData);
    }

    @Test
    void setSerializedData_stores_copy() {
        TabulatedFunctionDTO dto = new TabulatedFunctionDTO();
        byte[] input = {100, (byte) 200};
        dto.setSerializedData(input);

        input[0] = 0;
        byte[] stored = dto.getSerializedData();
        assertThat(stored[0]).isEqualTo((byte) 100); // не ссылка
    }

    // ========= fromResultSet =========
    @Test
    void fromResultSet_works() throws SQLException {
        ResultSet rs = mock(ResultSet.class);
        when(rs.getLong("id")).thenReturn(7L);
        when(rs.getLong("owner_id")).thenReturn(70L);
        when(rs.getLong("function_type_id")).thenReturn(4L);
        when(rs.getString("name")).thenReturn("rs_test");
        when(rs.getBytes("serialized_data")).thenReturn(sampleData);
        when(rs.getObject("created_at", LocalDateTime.class)).thenReturn(now);
        when(rs.getObject("updated_at", LocalDateTime.class)).thenReturn(now.plusMinutes(1));

        TabulatedFunctionDTO dto = TabulatedFunctionDTO.fromResultSet(rs);

        assertThat(dto.getId()).isEqualTo(7L);
        assertThat(dto.getOwnerId()).isEqualTo(70L);
        assertThat(dto.getFunctionTypeId()).isEqualTo(4L);
        assertThat(dto.getSerializedData()).isEqualTo(sampleData);
        assertThat(dto.getName()).isEqualTo("rs_test");
        assertThat(dto.getCreatedAt()).isEqualTo(now);
        assertThat(dto.getUpdatedAt()).isEqualTo(now.plusMinutes(1));
    }

    @Test
    void fromResultSet_handles_null_data() throws SQLException {
        ResultSet rs = mock(ResultSet.class);
        when(rs.getLong("id")).thenReturn(1L);
        when(rs.getString("name")).thenReturn("null_data");
        when(rs.getBytes("serialized_data")).thenReturn(null); // ← null в БД
        when(rs.getObject("created_at", LocalDateTime.class)).thenReturn(now);

        TabulatedFunctionDTO dto = TabulatedFunctionDTO.fromResultSet(rs);
        assertThat(dto.getSerializedData()).isNull();
    }

    // ========= equals / hashCode =========
    @Test
    void equalsAndHashCode_work() {
        TabulatedFunctionDTO dto1 = new TabulatedFunctionDTO(1L, 1L, 1L, sampleData, "same", now, now);
        TabulatedFunctionDTO dto2 = new TabulatedFunctionDTO(1L, 1L, 1L, Arrays.copyOf(sampleData, sampleData.length), "same", now, now);

        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
    }

    @Test
    void equals_differs_by_id() {
        TabulatedFunctionDTO d1 = new TabulatedFunctionDTO(1L, 1L, 1L, null, "f", null, null);
        TabulatedFunctionDTO d2 = new TabulatedFunctionDTO(2L, 1L, 1L, null, "f", null, null);
        assertThat(d1).isNotEqualTo(d2);
    }

    @Test
    void equals_differs_by_name() {
        TabulatedFunctionDTO d1 = new TabulatedFunctionDTO(1L, 1L, 1L, null, "f1", null, null);
        TabulatedFunctionDTO d2 = new TabulatedFunctionDTO(1L, 1L, 1L, null, "f2", null, null);
        assertThat(d1).isNotEqualTo(d2);
    }

    // ========= Edge cases =========
    @Test
    void handles_empty_byte_array() {
        TabulatedFunctionDTO dto = new TabulatedFunctionDTO(1L, 1L, new byte[0], "empty");
        assertThat(dto.getSerializedData()).hasSize(0);
    }

    @Test
    void handles_null_in_constructor() {
        TabulatedFunctionDTO dto = new TabulatedFunctionDTO(1L, 1L, null, "null_data");
        assertThat(dto.getSerializedData()).isNull();
    }
}