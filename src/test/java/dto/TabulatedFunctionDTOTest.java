package dto;

import functions.ArrayTabulatedFunction;
import functions.TabulatedFunction;
import io.FunctionsIO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TabulatedFunctionDTOTest {

    @Mock
    private ResultSet mockResultSet;

    private TabulatedFunction createTestFunction() {
        double[] xValues = {0.0, 1.0, 2.0};
        double[] yValues = {0.0, 1.0, 4.0};
        return new ArrayTabulatedFunction(xValues, yValues);
    }

    private byte[] createSerializedFunctionBytes() throws IOException {
        TabulatedFunction func = createTestFunction();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (java.io.BufferedOutputStream bos = new java.io.BufferedOutputStream(baos)) {
            FunctionsIO.serialize(bos, func);
        }
        return baos.toByteArray();
    }

    private byte[] createCorruptedSerializedData() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject("test");
        oos.close();
        byte[] data = baos.toByteArray();
        data[0] = 0;
        data[1] = 0;
        return data;
    }

    @Test
    void testPublicConstructor() {
        TabulatedFunctionDTO dto = new TabulatedFunctionDTO(1L, 2L, 3L, createTestFunction(), "Test Function",
                LocalDateTime.of(2024, 1, 1, 10, 0), LocalDateTime.of(2024, 1, 2, 15, 30));

        assertEquals(1L, dto.getId());
        assertEquals(2L, dto.getOwnerId());
        assertEquals(3L, dto.getFunctionTypeId());
        assertEquals("Test Function", dto.getName());
        assertNotNull(dto.getFunction());
        assertNotNull(dto.getSerializedData());
        assertTrue(dto.getSerializedData().length > 0);
    }

    @Test
    void testPublicConstructorWithNullFunction() {
        TabulatedFunctionDTO dto = new TabulatedFunctionDTO(1L, 2L, 3L, null, "Null Function",
                LocalDateTime.now(), LocalDateTime.now());

        assertEquals(1L, dto.getId());
        assertEquals(2L, dto.getOwnerId());
        assertEquals(3L, dto.getFunctionTypeId());
        assertEquals("Null Function", dto.getName());
        assertNull(dto.getFunction());
        assertNull(dto.getSerializedData());
    }

    @Test
    void testFromResultSet_Success() throws SQLException, IOException {
        byte[] serializedFuncBytes = createSerializedFunctionBytes();
        when(mockResultSet.getLong("id")).thenReturn(100L);
        when(mockResultSet.getLong("owner_id")).thenReturn(1L);
        when(mockResultSet.getLong("function_type_id")).thenReturn(10L);
        when(mockResultSet.getString("name")).thenReturn("MyFunction");
        when(mockResultSet.getBytes("serialized_data")).thenReturn(serializedFuncBytes);
        when(mockResultSet.getObject("created_at", LocalDateTime.class)).thenReturn(LocalDateTime.now());
        when(mockResultSet.getObject("updated_at", LocalDateTime.class)).thenReturn(LocalDateTime.now().plusMinutes(5));

        TabulatedFunctionDTO dto = TabulatedFunctionDTO.fromResultSet(mockResultSet);

        assertNotNull(dto);
        assertEquals(100L, dto.getId());
        assertEquals(1L, dto.getOwnerId());
        assertEquals(10L, dto.getFunctionTypeId());
        assertEquals("MyFunction", dto.getName());
        assertArrayEquals(serializedFuncBytes, dto.getSerializedData());
        assertNotNull(dto.getFunction());
    }

    @Test
    void testFromResultSet_NullFunctionData() throws SQLException {
        when(mockResultSet.getLong("id")).thenReturn(101L);
        when(mockResultSet.getLong("owner_id")).thenReturn(2L);
        when(mockResultSet.getLong("function_type_id")).thenReturn(11L);
        when(mockResultSet.getString("name")).thenReturn("EmptyFunction");
        when(mockResultSet.getBytes("serialized_data")).thenReturn(null);
        when(mockResultSet.getObject("created_at", LocalDateTime.class)).thenReturn(LocalDateTime.of(2023, 10, 1, 12, 0));
        when(mockResultSet.getObject("updated_at", LocalDateTime.class)).thenReturn(LocalDateTime.of(2023, 10, 1, 12, 5));

        TabulatedFunctionDTO dto = TabulatedFunctionDTO.fromResultSet(mockResultSet);

        assertNotNull(dto);
        assertEquals(101L, dto.getId());
        assertEquals(2L, dto.getOwnerId());
        assertEquals(11L, dto.getFunctionTypeId());
        assertEquals("EmptyFunction", dto.getName());
        assertNull(dto.getSerializedData());
        assertNull(dto.getFunction());
    }

    @Test
    void testFromResultSet_ThrowsSQLException() throws SQLException {
        when(mockResultSet.getLong("id")).thenThrow(new SQLException("Database error"));
        assertThrows(SQLException.class, () -> TabulatedFunctionDTO.fromResultSet(mockResultSet));
    }

    @Test
    void testFromResultSet_DeserializationError() throws SQLException, IOException {
        byte[] corruptedBytes = createCorruptedSerializedData();
        when(mockResultSet.getLong("id")).thenReturn(1L);
        when(mockResultSet.getLong("owner_id")).thenReturn(1L);
        when(mockResultSet.getLong("function_type_id")).thenReturn(1L);
        when(mockResultSet.getString("name")).thenReturn("Invalid Function");
        when(mockResultSet.getBytes("serialized_data")).thenReturn(corruptedBytes);
        when(mockResultSet.getObject("created_at", LocalDateTime.class)).thenReturn(LocalDateTime.now());
        when(mockResultSet.getObject("updated_at", LocalDateTime.class)).thenReturn(LocalDateTime.now());

        assertThrows(RuntimeException.class, () -> TabulatedFunctionDTO.fromResultSet(mockResultSet));
    }

    @Test
    void testSetters() {
        TabulatedFunctionDTO dto = new TabulatedFunctionDTO(1L, 1L, 1L, createTestFunction(), "Original",
                LocalDateTime.now(), LocalDateTime.now());

        dto.setId(2L);
        dto.setOwnerId(3L);
        dto.setFunctionTypeId(4L);
        dto.setName("Updated Name");
        LocalDateTime newCreatedAt = LocalDateTime.of(2024, 2, 1, 12, 0);
        LocalDateTime newUpdatedAt = LocalDateTime.of(2024, 2, 2, 18, 0);
        dto.setCreatedAt(newCreatedAt);
        dto.setUpdatedAt(newUpdatedAt);
        byte[] newSerializedData = "test data".getBytes();
        dto.setSerializedData(newSerializedData);

        assertEquals(2L, dto.getId());
        assertEquals(3L, dto.getOwnerId());
        assertEquals(4L, dto.getFunctionTypeId());
        assertEquals("Updated Name", dto.getName());
        assertEquals(newCreatedAt, dto.getCreatedAt());
        assertEquals(newUpdatedAt, dto.getUpdatedAt());
        assertArrayEquals(newSerializedData, dto.getSerializedData());
    }

    @Test
    void testSettersWithNullValues() {
        TabulatedFunctionDTO dto = new TabulatedFunctionDTO(1L, 1L, 1L, createTestFunction(), "Test",
                LocalDateTime.now(), LocalDateTime.now());

        dto.setId(null);
        dto.setOwnerId(null);
        dto.setFunctionTypeId(null);
        dto.setName(null);
        dto.setCreatedAt(null);
        dto.setUpdatedAt(null);
        dto.setSerializedData(null);

        assertNull(dto.getId());
        assertNull(dto.getOwnerId());
        assertNull(dto.getFunctionTypeId());
        assertNull(dto.getName());
        assertNull(dto.getCreatedAt());
        assertNull(dto.getUpdatedAt());
        assertNull(dto.getSerializedData());
    }

    @Test
    void testEqualsAndHashCode() {
        LocalDateTime now = LocalDateTime.now();
        TabulatedFunctionDTO dto1 = new TabulatedFunctionDTO(1L, 2L, 3L, createTestFunction(), "Function1", now, now);
        TabulatedFunctionDTO dto2 = new TabulatedFunctionDTO(1L, 2L, 3L, createTestFunction(), "Function1", now, now);
        TabulatedFunctionDTO dto3 = new TabulatedFunctionDTO(4L, 2L, 3L, createTestFunction(), "Function1", now, now);

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1, dto3);
        assertNotEquals(dto1.hashCode(), dto3.hashCode());
    }

    @Test
    void testEqualsWithDifferentFields() {
        LocalDateTime now = LocalDateTime.now();
        TabulatedFunctionDTO base = new TabulatedFunctionDTO(1L, 2L, 3L, createTestFunction(), "Function", now, now);
        TabulatedFunctionDTO diffOwner = new TabulatedFunctionDTO(1L, 5L, 3L, createTestFunction(), "Function", now, now);
        TabulatedFunctionDTO diffType = new TabulatedFunctionDTO(1L, 2L, 6L, createTestFunction(), "Function", now, now);
        TabulatedFunctionDTO diffName = new TabulatedFunctionDTO(1L, 2L, 3L, createTestFunction(), "Different", now, now);

        assertNotEquals(base, diffOwner);
        assertNotEquals(base, diffType);
        assertNotEquals(base, diffName);
    }

    @Test
    void testEqualsWithNullAndDifferentClass() {
        TabulatedFunctionDTO dto = new TabulatedFunctionDTO(1L, 1L, 1L, createTestFunction(), "Test",
                LocalDateTime.now(), LocalDateTime.now());

        assertNotEquals(null, dto);
        assertNotEquals(dto, new Object());
        assertEquals(dto, dto);
    }

    @Test
    void testToString() {
        TabulatedFunctionDTO dto = new TabulatedFunctionDTO(123L, 456L, 789L, createTestFunction(), "Test Function",
                LocalDateTime.of(2024, 1, 15, 9, 30), LocalDateTime.of(2024, 1, 20, 14, 45));

        String result = dto.toString();

        assertNotNull(result);
        assertTrue(result.contains("TabulatedFunctionDTO{"));
        assertTrue(result.contains("id=123"));
        assertTrue(result.contains("ownerId=456"));
        assertTrue(result.contains("functionTypeId=789"));
        assertTrue(result.contains("name='Test Function'"));
        assertTrue(result.contains("serializedDataLength="));
    }

    @Test
    void testToStringWithNullValues() {
        TabulatedFunctionDTO dto = new TabulatedFunctionDTO(null, null, null, null, null, null, null);

        String result = dto.toString();

        assertNotNull(result);
        assertTrue(result.contains("id=null"));
        assertTrue(result.contains("ownerId=null"));
        assertTrue(result.contains("functionTypeId=null"));
        assertTrue(result.contains("name='null'"));
        assertTrue(result.contains("createdAt=null"));
        assertTrue(result.contains("updatedAt=null"));
        assertTrue(result.contains("serializedDataLength=0"));
    }

    @Test
    void testSerializeFunction() {
        TabulatedFunctionDTO dto = new TabulatedFunctionDTO(1L, 1L, 1L, null, "Test",
                LocalDateTime.now(), LocalDateTime.now());
        byte[] result = dto.serializeFunction(createTestFunction());

        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void testSerializeFunctionWithNull() {
        TabulatedFunctionDTO dto = new TabulatedFunctionDTO(1L, 1L, 1L, null, "Test",
                LocalDateTime.now(), LocalDateTime.now());
        assertNull(dto.serializeFunction(null));
    }

    @Test
    void testSerializeFunctionWithDifferentData() {
        TabulatedFunctionDTO dto = new TabulatedFunctionDTO(1L, 1L, 1L, null, "Test",
                LocalDateTime.now(), LocalDateTime.now());

        double[] xValues = {1.0, 2.0, 3.0, 4.0};
        double[] yValues = {1.0, 8.0, 27.0, 64.0};
        TabulatedFunction differentFunction = new ArrayTabulatedFunction(xValues, yValues);

        byte[] result = dto.serializeFunction(differentFunction);
        byte[] standardResult = dto.serializeFunction(createTestFunction());

        assertNotNull(result);
        assertTrue(result.length > 0);
        assertNotEquals(standardResult.length, result.length);
    }

    @Test
    void testStaticDeserializeFunction() throws IOException, ClassNotFoundException {
        byte[] serializedData = createSerializedFunctionBytes();
        TabulatedFunction result = TabulatedFunctionDTO.deserializeFunction(serializedData);

        assertNotNull(result);
        assertEquals(3, result.getCount());
        assertEquals(0.0, result.getX(0), 0.0001);
        assertEquals(1.0, result.getX(1), 0.0001);
        assertEquals(2.0, result.getX(2), 0.0001);
    }

    @Test
    void testStaticDeserializeFunctionWithNull() throws IOException, ClassNotFoundException {
        assertNull(TabulatedFunctionDTO.deserializeFunction(null));
    }

    @Test
    void testStaticDeserializeFunctionWithInvalidData() {
        byte[] invalidData = "invalid serialized data".getBytes();
        assertThrows(IOException.class, () -> TabulatedFunctionDTO.deserializeFunction(invalidData));
    }

    @Test
    void testMultipleSetterCalls() {
        TabulatedFunctionDTO dto = new TabulatedFunctionDTO(1L, 1L, 1L, createTestFunction(), "Original",
                LocalDateTime.now(), LocalDateTime.now());

        dto.setId(10L);
        dto.setId(20L);
        dto.setOwnerId(30L);
        dto.setOwnerId(40L);
        dto.setFunctionTypeId(50L);
        dto.setFunctionTypeId(60L);
        dto.setName("First");
        dto.setName("Second");
        LocalDateTime time1 = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime time2 = LocalDateTime.of(2024, 1, 2, 0, 0);
        dto.setCreatedAt(time1);
        dto.setCreatedAt(time2);
        dto.setUpdatedAt(time1);
        dto.setUpdatedAt(time2);
        byte[] data1 = "data1".getBytes();
        byte[] data2 = "data2".getBytes();
        dto.setSerializedData(data1);
        dto.setSerializedData(data2);

        assertEquals(20L, dto.getId());
        assertEquals(40L, dto.getOwnerId());
        assertEquals(60L, dto.getFunctionTypeId());
        assertEquals("Second", dto.getName());
        assertEquals(time2, dto.getCreatedAt());
        assertEquals(time2, dto.getUpdatedAt());
        assertArrayEquals(data2, dto.getSerializedData());
    }

    @Test
    void testNameWithSpecialCharacters() {
        TabulatedFunctionDTO dto = new TabulatedFunctionDTO(1L, 1L, 1L, createTestFunction(), "Original",
                LocalDateTime.now(), LocalDateTime.now());

        dto.setName("");
        assertEquals("", dto.getName());

        dto.setName("Function@123#test$");
        assertEquals("Function@123#test$", dto.getName());

        dto.setName("My Test Function");
        assertEquals("My Test Function", dto.getName());
    }
}