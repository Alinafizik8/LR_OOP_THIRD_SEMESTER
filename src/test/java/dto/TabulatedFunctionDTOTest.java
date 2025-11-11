//package dto;
//
//import functions.ArrayTabulatedFunction;
//import functions.TabulatedFunction;
//import io.FunctionsIO;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.io.ByteArrayInputStream;
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.io.ObjectOutputStream;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.time.LocalDateTime;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class TabulatedFunctionDTOTest {
//
//    @Mock
//    private ResultSet mockResultSet;
//
//    private TabulatedFunction createTestFunction() {
//        double[] xValues = {0.0, 1.0, 2.0};
//        double[] yValues = {0.0, 1.0, 4.0};
//        return new ArrayTabulatedFunction(xValues, yValues);
//    }
//
//    private byte[] createSerializedFunctionBytes() throws IOException {
//        TabulatedFunction func = createTestFunction();
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        try (java.io.BufferedOutputStream bos = new java.io.BufferedOutputStream(baos)) {
//            FunctionsIO.serialize(bos, func);
//        }
//        return baos.toByteArray();
//    }
//
//    private byte[] createCorruptedSerializedData() throws IOException {
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        ObjectOutputStream oos = new ObjectOutputStream(baos);
//        oos.writeObject("test");
//        oos.close();
//        byte[] data = baos.toByteArray();
//        data[0] = 0;
//        data[1] = 0;
//        return data;
//    }
//
//    @Test
//    void testPublicConstructor() {
//        TabulatedFunctionDTO dto = new TabulatedFunctionDTO(1L, 2L, 3L, createTestFunction(), "Test Function",
//                LocalDateTime.of(2024, 1, 1, 10, 0), LocalDateTime.of(2024, 1, 2, 15, 30));
//
//        assertEquals(1L, dto.getId());
//        assertEquals(2L, dto.getOwnerId());
//        assertEquals(3L, dto.getFunctionTypeId());
//        assertEquals("Test Function", dto.getName());
//        assertNotNull(dto.getFunction());
//        assertNotNull(dto.getSerializedData());
//        assertTrue(dto.getSerializedData().length > 0);
//    }
//
//    @Test
//    void testPublicConstructorWithNullFunction() {
//        TabulatedFunctionDTO dto = new TabulatedFunctionDTO(1L, 2L, 3L, null, "Null Function",
//                LocalDateTime.now(), LocalDateTime.now());
//
//        assertEquals(1L, dto.getId());
//        assertEquals(2L, dto.getOwnerId());
//        assertEquals(3L, dto.getFunctionTypeId());
//        assertEquals("Null Function", dto.getName());
//        assertNull(dto.getFunction());
//        assertNull(dto.getSerializedData());
//    }
//
//    @Test
//    void testFromResultSet_Success() throws SQLException, IOException {
//        byte[] serializedFuncBytes = createSerializedFunctionBytes();
//        when(mockResultSet.getLong("id")).thenReturn(100L);
//        when(mockResultSet.getLong("owner_id")).thenReturn(1L);
//        when(mockResultSet.getLong("function_type_id")).thenReturn(10L);
//        when(mockResultSet.getString("name")).thenReturn("MyFunction");
//        when(mockResultSet.getBytes("serialized_data")).thenReturn(serializedFuncBytes);
//        when(mockResultSet.getObject("created_at", LocalDateTime.class)).thenReturn(LocalDateTime.now());
//        when(mockResultSet.getObject("updated_at", LocalDateTime.class)).thenReturn(LocalDateTime.now().plusMinutes(5));
//
//        TabulatedFunctionDTO dto = TabulatedFunctionDTO.fromResultSet(mockResultSet);
//
//        assertNotNull(dto);
//        assertEquals(100L, dto.getId());
//        assertEquals(1L, dto.getOwnerId());
//        assertEquals(10L, dto.getFunctionTypeId());
//        assertEquals("MyFunction", dto.getName());
//        assertArrayEquals(serializedFuncBytes, dto.getSerializedData());
//        assertNotNull(dto.getFunction());
//    }
//
//    @Test
//    void testFromResultSet_NullFunctionData() throws SQLException {
//        when(mockResultSet.getLong("id")).thenReturn(101L);
//        when(mockResultSet.getLong("owner_id")).thenReturn(2L);
//        when(mockResultSet.getLong("function_type_id")).thenReturn(11L);
//        when(mockResultSet.getString("name")).thenReturn("EmptyFunction");
//        when(mockResultSet.getBytes("serialized_data")).thenReturn(null);
//        when(mockResultSet.getObject("created_at", LocalDateTime.class)).thenReturn(LocalDateTime.of(2023, 10, 1, 12, 0));
//        when(mockResultSet.getObject("updated_at", LocalDateTime.class)).thenReturn(LocalDateTime.of(2023, 10, 1, 12, 5));
//
//        TabulatedFunctionDTO dto = TabulatedFunctionDTO.fromResultSet(mockResultSet);
//
//        assertNotNull(dto);
//        assertEquals(101L, dto.getId());
//        assertEquals(2L, dto.getOwnerId());
//        assertEquals(11L, dto.getFunctionTypeId());
//        assertEquals("EmptyFunction", dto.getName());
//        assertNull(dto.getSerializedData());
//        assertNull(dto.getFunction());
//    }
//
//    @Test
//    void testFromResultSet_ThrowsSQLException() throws SQLException {
//        when(mockResultSet.getLong("id")).thenThrow(new SQLException("Database error"));
//        assertThrows(SQLException.class, () -> TabulatedFunctionDTO.fromResultSet(mockResultSet));
//    }
//
//    @Test
//    void testFromResultSet_DeserializationError() throws SQLException, IOException {
//        byte[] corruptedBytes = createCorruptedSerializedData();
//        when(mockResultSet.getLong("id")).thenReturn(1L);
//        when(mockResultSet.getLong("owner_id")).thenReturn(1L);
//        when(mockResultSet.getLong("function_type_id")).thenReturn(1L);
//        when(mockResultSet.getString("name")).thenReturn("Invalid Function");
//        when(mockResultSet.getBytes("serialized_data")).thenReturn(corruptedBytes);
//        when(mockResultSet.getObject("created_at", LocalDateTime.class)).thenReturn(LocalDateTime.now());
//        when(mockResultSet.getObject("updated_at", LocalDateTime.class)).thenReturn(LocalDateTime.now());
//
//        assertThrows(RuntimeException.class, () -> TabulatedFunctionDTO.fromResultSet(mockResultSet));
//    }
//
//    @Test
//    void testSetters() {
//        TabulatedFunctionDTO dto = new TabulatedFunctionDTO(1L, 1L, 1L, createTestFunction(), "Original",
//                LocalDateTime.now(), LocalDateTime.now());
//
//        dto.setId(2L);
//        dto.setOwnerId(3L);
//        dto.setFunctionTypeId(4L);
//        dto.setName("Updated Name");
//        LocalDateTime newCreatedAt = LocalDateTime.of(2024, 2, 1, 12, 0);
//        LocalDateTime newUpdatedAt = LocalDateTime.of(2024, 2, 2, 18, 0);
//        dto.setCreatedAt(newCreatedAt);
//        dto.setUpdatedAt(newUpdatedAt);
//        byte[] newSerializedData = "test data".getBytes();
//        dto.setSerializedData(newSerializedData);
//
//        assertEquals(2L, dto.getId());
//        assertEquals(3L, dto.getOwnerId());
//        assertEquals(4L, dto.getFunctionTypeId());
//        assertEquals("Updated Name", dto.getName());
//        assertEquals(newCreatedAt, dto.getCreatedAt());
//        assertEquals(newUpdatedAt, dto.getUpdatedAt());
//        assertArrayEquals(newSerializedData, dto.getSerializedData());
//    }
//
//    @Test
//    void testSettersWithNullValues() {
//        TabulatedFunctionDTO dto = new TabulatedFunctionDTO(1L, 1L, 1L, createTestFunction(), "Test",
//                LocalDateTime.now(), LocalDateTime.now());
//
//        dto.setId(null);
//        dto.setOwnerId(null);
//        dto.setFunctionTypeId(null);
//        dto.setName(null);
//        dto.setCreatedAt(null);
//        dto.setUpdatedAt(null);
//        dto.setSerializedData(null);
//
//        assertNull(dto.getId());
//        assertNull(dto.getOwnerId());
//        assertNull(dto.getFunctionTypeId());
//        assertNull(dto.getName());
//        assertNull(dto.getCreatedAt());
//        assertNull(dto.getUpdatedAt());
//        assertNull(dto.getSerializedData());
//    }
//
//    @Test
//    void testEqualsAndHashCode() {
//        LocalDateTime now = LocalDateTime.now();
//        TabulatedFunctionDTO dto1 = new TabulatedFunctionDTO(1L, 2L, 3L, createTestFunction(), "Function1", now, now);
//        TabulatedFunctionDTO dto2 = new TabulatedFunctionDTO(1L, 2L, 3L, createTestFunction(), "Function1", now, now);
//        TabulatedFunctionDTO dto3 = new TabulatedFunctionDTO(4L, 2L, 3L, createTestFunction(), "Function1", now, now);
//
//        assertEquals(dto1, dto2);
//        assertEquals(dto1.hashCode(), dto2.hashCode());
//        assertNotEquals(dto1, dto3);
//        assertNotEquals(dto1.hashCode(), dto3.hashCode());
//    }
//
//    @Test
//    void testEqualsWithDifferentFields() {
//        LocalDateTime now = LocalDateTime.now();
//        TabulatedFunctionDTO base = new TabulatedFunctionDTO(1L, 2L, 3L, createTestFunction(), "Function", now, now);
//        TabulatedFunctionDTO diffOwner = new TabulatedFunctionDTO(1L, 5L, 3L, createTestFunction(), "Function", now, now);
//        TabulatedFunctionDTO diffType = new TabulatedFunctionDTO(1L, 2L, 6L, createTestFunction(), "Function", now, now);
//        TabulatedFunctionDTO diffName = new TabulatedFunctionDTO(1L, 2L, 3L, createTestFunction(), "Different", now, now);
//
//        assertNotEquals(base, diffOwner);
//        assertNotEquals(base, diffType);
//        assertNotEquals(base, diffName);
//    }
//
//    @Test
//    void testEqualsWithNullAndDifferentClass() {
//        TabulatedFunctionDTO dto = new TabulatedFunctionDTO(1L, 1L, 1L, createTestFunction(), "Test",
//                LocalDateTime.now(), LocalDateTime.now());
//
//        assertNotEquals(null, dto);
//        assertNotEquals(dto, new Object());
//        assertEquals(dto, dto);
//    }
//
//    @Test
//    void testToString() {
//        TabulatedFunctionDTO dto = new TabulatedFunctionDTO(123L, 456L, 789L, createTestFunction(), "Test Function",
//                LocalDateTime.of(2024, 1, 15, 9, 30), LocalDateTime.of(2024, 1, 20, 14, 45));
//
//        String result = dto.toString();
//
//        assertNotNull(result);
//        assertTrue(result.contains("TabulatedFunctionDTO{"));
//        assertTrue(result.contains("id=123"));
//        assertTrue(result.contains("ownerId=456"));
//        assertTrue(result.contains("functionTypeId=789"));
//        assertTrue(result.contains("name='Test Function'"));
//        assertTrue(result.contains("serializedDataLength="));
//    }
//
//    @Test
//    void testToStringWithNullValues() {
//        TabulatedFunctionDTO dto = new TabulatedFunctionDTO(null, null, null, null, null, null, null);
//
//        String result = dto.toString();
//
//        assertNotNull(result);
//        assertTrue(result.contains("id=null"));
//        assertTrue(result.contains("ownerId=null"));
//        assertTrue(result.contains("functionTypeId=null"));
//        assertTrue(result.contains("name='null'"));
//        assertTrue(result.contains("createdAt=null"));
//        assertTrue(result.contains("updatedAt=null"));
//        assertTrue(result.contains("serializedDataLength=0"));
//    }
//
//    @Test
//    void testSerializeFunction() {
//        TabulatedFunctionDTO dto = new TabulatedFunctionDTO(1L, 1L, 1L, null, "Test",
//                LocalDateTime.now(), LocalDateTime.now());
//        byte[] result = dto.serializeFunction(createTestFunction());
//
//        assertNotNull(result);
//        assertTrue(result.length > 0);
//    }
//
//    @Test
//    void testSerializeFunctionWithNull() {
//        TabulatedFunctionDTO dto = new TabulatedFunctionDTO(1L, 1L, 1L, null, "Test",
//                LocalDateTime.now(), LocalDateTime.now());
//        assertNull(dto.serializeFunction(null));
//    }
//
//    @Test
//    void testSerializeFunctionWithDifferentData() {
//        TabulatedFunctionDTO dto = new TabulatedFunctionDTO(1L, 1L, 1L, null, "Test",
//                LocalDateTime.now(), LocalDateTime.now());
//
//        double[] xValues = {1.0, 2.0, 3.0, 4.0};
//        double[] yValues = {1.0, 8.0, 27.0, 64.0};
//        TabulatedFunction differentFunction = new ArrayTabulatedFunction(xValues, yValues);
//
//        byte[] result = dto.serializeFunction(differentFunction);
//        byte[] standardResult = dto.serializeFunction(createTestFunction());
//
//        assertNotNull(result);
//        assertTrue(result.length > 0);
//        assertNotEquals(standardResult.length, result.length);
//    }
//
//    @Test
//    void testStaticDeserializeFunction() throws IOException, ClassNotFoundException {
//        byte[] serializedData = createSerializedFunctionBytes();
//        TabulatedFunction result = TabulatedFunctionDTO.deserializeFunction(serializedData);
//
//        assertNotNull(result);
//        assertEquals(3, result.getCount());
//        assertEquals(0.0, result.getX(0), 0.0001);
//        assertEquals(1.0, result.getX(1), 0.0001);
//        assertEquals(2.0, result.getX(2), 0.0001);
//    }
//
//    @Test
//    void testStaticDeserializeFunctionWithNull() throws IOException, ClassNotFoundException {
//        assertNull(TabulatedFunctionDTO.deserializeFunction(null));
//    }
//
//    @Test
//    void testStaticDeserializeFunctionWithInvalidData() {
//        byte[] invalidData = "invalid serialized data".getBytes();
//        assertThrows(IOException.class, () -> TabulatedFunctionDTO.deserializeFunction(invalidData));
//    }
//
//    @Test
//    void testMultipleSetterCalls() {
//        TabulatedFunctionDTO dto = new TabulatedFunctionDTO(1L, 1L, 1L, createTestFunction(), "Original",
//                LocalDateTime.now(), LocalDateTime.now());
//
//        dto.setId(10L);
//        dto.setId(20L);
//        dto.setOwnerId(30L);
//        dto.setOwnerId(40L);
//        dto.setFunctionTypeId(50L);
//        dto.setFunctionTypeId(60L);
//        dto.setName("First");
//        dto.setName("Second");
//        LocalDateTime time1 = LocalDateTime.of(2024, 1, 1, 0, 0);
//        LocalDateTime time2 = LocalDateTime.of(2024, 1, 2, 0, 0);
//        dto.setCreatedAt(time1);
//        dto.setCreatedAt(time2);
//        dto.setUpdatedAt(time1);
//        dto.setUpdatedAt(time2);
//        byte[] data1 = "data1".getBytes();
//        byte[] data2 = "data2".getBytes();
//        dto.setSerializedData(data1);
//        dto.setSerializedData(data2);
//
//        assertEquals(20L, dto.getId());
//        assertEquals(40L, dto.getOwnerId());
//        assertEquals(60L, dto.getFunctionTypeId());
//        assertEquals("Second", dto.getName());
//        assertEquals(time2, dto.getCreatedAt());
//        assertEquals(time2, dto.getUpdatedAt());
//        assertArrayEquals(data2, dto.getSerializedData());
//    }
//
//    @Test
//    void testNameWithSpecialCharacters() {
//        TabulatedFunctionDTO dto = new TabulatedFunctionDTO(1L, 1L, 1L, createTestFunction(), "Original",
//                LocalDateTime.now(), LocalDateTime.now());
//
//        dto.setName("");
//        assertEquals("", dto.getName());
//
//        dto.setName("Function@123#test$");
//        assertEquals("Function@123#test$", dto.getName());
//
//        dto.setName("My Test Function");
//        assertEquals("My Test Function", dto.getName());
//    }
//}
//package dto;
//
//import functions.LinkedListTabulatedFunction;
//import functions.TabulatedFunction;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.sql.Timestamp;
//import java.time.LocalDateTime;
//import java.util.Arrays;
//
//import static org.assertj.core.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//class TabulatedFunctionDTOTest {
//    private static final Logger logger = LoggerFactory.getLogger(TabulatedFunctionDTOTest.class);
//
//    private TabulatedFunction testFunction;
//    private byte[] serializedTestData;
//
//    @BeforeEach
//    void setUp() {
//        // Создаём табулированную функцию f(x) = x^2 на [0, 2]
//        double[] x = {0.0, 1.0, 2.0};
//        double[] y = {0.0, 1.0, 4.0};
//        testFunction = new LinkedListTabulatedFunction(x, y);
//        serializedTestData = TabulatedFunctionDTO.serializeFunction(testFunction);
//        logger.debug("Test function serialized: {} bytes", serializedTestData.length);
//    }
//
//    // ========= CONSTRUCTORS =========
//    @Test
//    void fullConstructor_works() {
//        LocalDateTime now = LocalDateTime.now();
//        TabulatedFunctionDTO dto = new TabulatedFunctionDTO(
//                1L, 100L, 2L, serializedTestData, "test_func", now, now.plusSeconds(1)
//        );
//
//        assertThat(dto.getId()).isEqualTo(1L);
//        assertThat(dto.getOwnerId()).isEqualTo(100L);
//        assertThat(dto.getFunctionTypeId()).isEqualTo(2L);
//        assertThat(dto.getSerializedData()).isEqualTo(serializedTestData);
//        assertThat(dto.getName()).isEqualTo("test_func");
//        assertThat(dto.getCreatedAt()).isEqualTo(now);
//        assertThat(dto.getUpdatedAt()).isEqualTo(now.plusSeconds(1));
//    }
//
//    @Test
//    void minimalConstructor_works() {
//        TabulatedFunctionDTO dto = new TabulatedFunctionDTO(10L, 20L, serializedTestData, "func");
//
//        assertThat(dto.getId()).isNull();
//        assertThat(dto.getOwnerId()).isEqualTo(10L);
//        assertThat(dto.getFunctionTypeId()).isEqualTo(20L);
//        assertThat(dto.getSerializedData()).isEqualTo(serializedTestData);
//        assertThat(dto.getName()).isEqualTo("func");
//        assertThat(dto.getCreatedAt()).isNull();
//        assertThat(dto.getUpdatedAt()).isNull();
//    }
//
//    @Test
//    void defaultConstructor_works() {
//        TabulatedFunctionDTO dto = new TabulatedFunctionDTO();
//        assertThat(dto).isNotNull();
//    }
//
//    // ========= GETTERS/SETTERS =========
//    @Test
//    void gettersAndSetters_work() {
//        TabulatedFunctionDTO dto = new TabulatedFunctionDTO();
//
//        dto.setId(5L);
//        dto.setOwnerId(50L);
//        dto.setFunctionTypeId(3L);
//        dto.setSerializedData(new byte[]{1, 2, 3});
//        dto.setName("setter_func");
//        dto.setCreatedAt(LocalDateTime.of(2025, 1, 1, 12, 0));
//        dto.setUpdatedAt(LocalDateTime.of(2025, 1, 1, 12, 1));
//
//        assertThat(dto.getId()).isEqualTo(5L);
//        assertThat(dto.getOwnerId()).isEqualTo(50L);
//        assertThat(dto.getFunctionTypeId()).isEqualTo(3L);
//        assertThat(dto.getSerializedData()).containsExactly(1, 2, 3);
//        assertThat(dto.getName()).isEqualTo("setter_func");
//        assertThat(dto.getCreatedAt()).isEqualTo(LocalDateTime.of(2025, 1, 1, 12, 0));
//        assertThat(dto.getUpdatedAt()).isEqualTo(LocalDateTime.of(2025, 1, 1, 12, 1));
//    }
//
//    @Test
//    void getSerializedData_returns_copy() {
//        TabulatedFunctionDTO dto = new TabulatedFunctionDTO(1L, 1L, serializedTestData, "copy");
//        byte[] original = dto.getSerializedData();
//        original[0] = (byte) 0xFF; // меняем копию
//
//        byte[] second = dto.getSerializedData();
//        assertThat(second[0]).isNotEqualTo((byte) 0xFF); // оригинал не изменился
//        assertThat(second).isEqualTo(serializedTestData);
//    }
//
//    @Test
//    void setSerializedData_stores_copy() {
//        TabulatedFunctionDTO dto = new TabulatedFunctionDTO();
//        byte[] input = {10, 20, 30};
//        dto.setSerializedData(input);
//
//        input[0] = 99;
//        byte[] stored = dto.getSerializedData();
//        assertThat(stored[0]).isEqualTo((byte) 10); // не ссылка на входной массив
//    }
//
//    // ========= UTILS: serializeFunction =========
//    @Test
//    void serializeFunction_works() {
//        byte[] data = TabulatedFunctionDTO.serializeFunction(testFunction);
//        assertThat(data).isNotNull();
//        assertThat(data.length).isGreaterThan(0);
//    }
//
//    @Test
//    void serializeFunction_null_returns_null() {
//        byte[] data = TabulatedFunctionDTO.serializeFunction(null);
//        assertThat(data).isNull();
//    }
//
//    @Test
//    void serializeFunction_logs_debug() {
//        // Используем реальный логгер — проверим через Appender (упрощённо)
//        assertThatCode(() -> TabulatedFunctionDTO.serializeFunction(testFunction))
//                .doesNotThrowAnyException();
//    }
//
//    // ========= UTILS: deserializeFunction =========
//    @Test
//    void deserializeFunction_works() {
//        TabulatedFunction func = TabulatedFunctionDTO.deserializeFunction(serializedTestData);
//        assertThat(func).isNotNull();
//        assertThat(func.getCount()).isEqualTo(3);
//        assertThat(func.getX(0)).isEqualTo(0.0);
//        assertThat(func.getY(2)).isEqualTo(4.0);
//    }
//
//    @Test
//    void deserializeFunction_null_returns_null() {
//        assertThat(TabulatedFunctionDTO.deserializeFunction(null)).isNull();
//        assertThat(TabulatedFunctionDTO.deserializeFunction(new byte[0])).isNull();
//    }
//
//    @Test
//    void deserializeFunction_invalid_data_throws() {
//        byte[] corrupted = {0x00, 0x01, 0x02}; // не сериализованный объект
//        assertThatThrownBy(() -> TabulatedFunctionDTO.deserializeFunction(corrupted))
//                .isInstanceOf(RuntimeException.class)
//                .hasMessageContaining("Deserialization failed");
//    }
//
//    // ========= FACTORY: fromResultSet =========
//    @Test
//    void fromResultSet_works() throws SQLException {
//        ResultSet rs = mock(ResultSet.class);
//        when(rs.getLong("id")).thenReturn(7L);
//        when(rs.getLong("owner_id")).thenReturn(70L);
//        when(rs.getLong("function_type_id")).thenReturn(4L);
//        when(rs.getString("name")).thenReturn("rs_func");
//        when(rs.getBytes("serialized_data")).thenReturn(serializedTestData);
//        when(rs.getObject("created_at", LocalDateTime.class))
//                .thenReturn(LocalDateTime.of(2025, 11, 11, 10, 0));
//        when(rs.getObject("updated_at", LocalDateTime.class))
//                .thenReturn(LocalDateTime.of(2025, 11, 11, 10, 1));
//
//        TabulatedFunctionDTO dto = TabulatedFunctionDTO.fromResultSet(rs);
//
//        assertThat(dto.getId()).isEqualTo(7L);
//        assertThat(dto.getOwnerId()).isEqualTo(70L);
//        assertThat(dto.getFunctionTypeId()).isEqualTo(4L);
//        assertThat(dto.getSerializedData()).isEqualTo(serializedTestData);
//        assertThat(dto.getName()).isEqualTo("rs_func");
//        assertThat(dto.getCreatedAt()).isEqualTo(LocalDateTime.of(2025, 11, 11, 10, 0));
//        assertThat(dto.getUpdatedAt()).isEqualTo(LocalDateTime.of(2025, 11, 11, 10, 1));
//    }
//
//    @Test
//    void fromResultSet_null_serializedData_works() throws SQLException {
//        ResultSet rs = mock(ResultSet.class);
//        when(rs.getLong("id")).thenReturn(1L);
//        when(rs.getLong("owner_id")).thenReturn(1L);
//        when(rs.getLong("function_type_id")).thenReturn(1L);
//        when(rs.getString("name")).thenReturn("null_data");
//        when(rs.getBytes("serialized_data")).thenReturn(null); // ← null
//        when(rs.getObject("created_at", LocalDateTime.class)).thenReturn(LocalDateTime.now());
//        when(rs.getObject("updated_at", LocalDateTime.class)).thenReturn(LocalDateTime.now());
//
//        TabulatedFunctionDTO dto = TabulatedFunctionDTO.fromResultSet(rs);
//        assertThat(dto.getSerializedData()).isNull();
//    }
//
//    // ========= EQUALS / HASHCODE =========
//    @Test
//    void equalsAndHashCode_work() {
//        TabulatedFunctionDTO dto1 = new TabulatedFunctionDTO(
//                1L, 1L, 1L, serializedTestData, "same", LocalDateTime.now(), LocalDateTime.now()
//        );
//        TabulatedFunctionDTO dto2 = new TabulatedFunctionDTO(
//                1L, 1L, 1L, Arrays.copyOf(serializedTestData, serializedTestData.length), "same",
//                dto1.getCreatedAt(), dto1.getUpdatedAt()
//        );
//
//        assertThat(dto1).isEqualTo(dto2);
//        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
//    }
//
//    @Test
//    void equals_differs_by_id() {
//        TabulatedFunctionDTO dto1 = new TabulatedFunctionDTO(1L, 1L, 1L, null, "f", null, null);
//        TabulatedFunctionDTO dto2 = new TabulatedFunctionDTO(2L, 1L, 1L, null, "f", null, null);
//        assertThat(dto1).isNotEqualTo(dto2);
//    }
//
//    @Test
//    void equals_differs_by_name() {
//        TabulatedFunctionDTO dto1 = new TabulatedFunctionDTO(1L, 1L, 1L, null, "f1", null, null);
//        TabulatedFunctionDTO dto2 = new TabulatedFunctionDTO(1L, 1L, 1L, null, "f2", null, null);
//        assertThat(dto1).isNotEqualTo(dto2);
//    }
//
//    // ========= TOSTRING =========
//    @Test
//    void toString_works() {
//        TabulatedFunctionDTO dto = new TabulatedFunctionDTO(5L, 5L, serializedTestData, "str_test");
//        String s = dto.toString();
//
//        assertThat(s).contains("id=5");
//        assertThat(s).contains("name='str_test'");
//        assertThat(s).contains("dataLength=" + serializedTestData.length);
//    }
//
//    // ========= EDGE CASES =========
//    @Test
//    void serialize_deserialize_roundtrip() {
//        byte[] data = TabulatedFunctionDTO.serializeFunction(testFunction);
//        TabulatedFunction restored = TabulatedFunctionDTO.deserializeFunction(data);
//
//        assertThat(restored).isNotNull();
//        assertThat(restored.getCount()).isEqualTo(testFunction.getCount());
//        for (int i = 0; i < testFunction.getCount(); i++) {
//            assertThat(restored.getX(i)).isEqualTo(testFunction.getX(i));
//            assertThat(restored.getY(i)).isEqualTo(testFunction.getY(i));
//        }
//    }
//
//    @Test
//    void fromResultSet_logs_debug() throws SQLException {
//        ResultSet rs = mock(ResultSet.class);
//        when(rs.getLong("id")).thenReturn(1L);
//        when(rs.getString("name")).thenReturn("log_test");
//        when(rs.getBytes("serialized_data")).thenReturn(serializedTestData);
//        // остальное — default null
//
//        TabulatedFunctionDTO dto = TabulatedFunctionDTO.fromResultSet(rs);
//        assertThat(dto).isNotNull();
//        // Логирование проверяется вручную при запуске тестов
//    }
//}
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