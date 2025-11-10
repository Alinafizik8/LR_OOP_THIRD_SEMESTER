package dto;

import functions.ArrayTabulatedFunction;
import functions.LinkedListTabulatedFunction;
import functions.TabulatedFunction;
import io.FunctionsIO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
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
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        try (java.io.BufferedOutputStream bos = new java.io.BufferedOutputStream(baos)) {
            FunctionsIO.serialize(bos, func);
        }
        return baos.toByteArray();
    }

    @Test
    void testPublicConstructor() {
        Long id = 1L;
        Long ownerId = 2L;
        Long functionTypeId = 3L;
        TabulatedFunction function = createTestFunction();
        String name = "Test Function";
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2024, 1, 2, 15, 30);

        TabulatedFunctionDTO dto = new TabulatedFunctionDTO(id, ownerId, functionTypeId, function, name, createdAt, updatedAt);

        assertEquals(id, dto.getId());
        assertEquals(ownerId, dto.getOwnerId());
        assertEquals(functionTypeId, dto.getFunctionTypeId());
        assertEquals(name, dto.getName());
        assertEquals(createdAt, dto.getCreatedAt());
        assertEquals(updatedAt, dto.getUpdatedAt());
        assertEquals(function, dto.getFunction());
        assertNotNull(dto.getSerializedData());
        assertTrue(dto.getSerializedData().length > 0);
    }

    @Test
    void testPublicConstructorWithNullFunction() {
        Long id = 1L;
        Long ownerId = 2L;
        Long functionTypeId = 3L;
        String name = "Null Function";
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = LocalDateTime.now();

        TabulatedFunctionDTO dto = new TabulatedFunctionDTO(id, ownerId, functionTypeId, null, name, createdAt, updatedAt);

        assertEquals(id, dto.getId());
        assertEquals(ownerId, dto.getOwnerId());
        assertEquals(functionTypeId, dto.getFunctionTypeId());
        assertEquals(name, dto.getName());
        assertNull(dto.getFunction());
        assertNull(dto.getSerializedData());
    }

    @Test
    void testFromResultSet_Success() throws SQLException, IOException {
        byte[] serializedFuncBytes = createSerializedFunctionBytes();
        long expectedId = 100L;
        long expectedOwnerId = 1L;
        long expectedTypeId = 10L;
        String expectedName = "MyFunction";
        LocalDateTime expectedCreatedAt = LocalDateTime.now();
        LocalDateTime expectedUpdatedAt = LocalDateTime.now().plusMinutes(5);

        when(mockResultSet.getLong("id")).thenReturn(expectedId);
        when(mockResultSet.getLong("owner_id")).thenReturn(expectedOwnerId);
        when(mockResultSet.getLong("function_type_id")).thenReturn(expectedTypeId);
        when(mockResultSet.getString("name")).thenReturn(expectedName);
        when(mockResultSet.getBytes("serialized_data")).thenReturn(serializedFuncBytes);
        when(mockResultSet.getObject("created_at", LocalDateTime.class)).thenReturn(expectedCreatedAt);
        when(mockResultSet.getObject("updated_at", LocalDateTime.class)).thenReturn(expectedUpdatedAt);

        TabulatedFunctionDTO dto = TabulatedFunctionDTO.fromResultSet(mockResultSet);

        assertNotNull(dto);
        assertEquals(expectedId, dto.getId());
        assertEquals(expectedOwnerId, dto.getOwnerId());
        assertEquals(expectedTypeId, dto.getFunctionTypeId());
        assertEquals(expectedName, dto.getName());
        assertArrayEquals(serializedFuncBytes, dto.getSerializedData());
        assertEquals(expectedCreatedAt, dto.getCreatedAt());
        assertEquals(expectedUpdatedAt, dto.getUpdatedAt());
        assertNotNull(dto.getFunction());

        verify(mockResultSet, times(1)).getLong("id");
        verify(mockResultSet, times(1)).getLong("owner_id");
        verify(mockResultSet, times(1)).getLong("function_type_id");
        verify(mockResultSet, times(1)).getString("name");
        verify(mockResultSet, times(1)).getBytes("serialized_data");
        verify(mockResultSet, times(1)).getObject("created_at", LocalDateTime.class);
        verify(mockResultSet, times(1)).getObject("updated_at", LocalDateTime.class);
    }

    @Test
    void testFromResultSet_NullFunctionData() throws SQLException {
        long expectedId = 101L;
        long expectedOwnerId = 2L;
        long expectedTypeId = 11L;
        String expectedName = "EmptyFunction";
        LocalDateTime expectedCreatedAt = LocalDateTime.of(2023, 10, 1, 12, 0);
        LocalDateTime expectedUpdatedAt = LocalDateTime.of(2023, 10, 1, 12, 5);

        when(mockResultSet.getLong("id")).thenReturn(expectedId);
        when(mockResultSet.getLong("owner_id")).thenReturn(expectedOwnerId);
        when(mockResultSet.getLong("function_type_id")).thenReturn(expectedTypeId);
        when(mockResultSet.getString("name")).thenReturn(expectedName);
        when(mockResultSet.getBytes("serialized_data")).thenReturn(null);
        when(mockResultSet.getObject("created_at", LocalDateTime.class)).thenReturn(expectedCreatedAt);
        when(mockResultSet.getObject("updated_at", LocalDateTime.class)).thenReturn(expectedUpdatedAt);

        TabulatedFunctionDTO dto = TabulatedFunctionDTO.fromResultSet(mockResultSet);

        assertNotNull(dto);
        assertEquals(expectedId, dto.getId());
        assertEquals(expectedOwnerId, dto.getOwnerId());
        assertEquals(expectedTypeId, dto.getFunctionTypeId());
        assertEquals(expectedName, dto.getName());
        assertNull(dto.getSerializedData());
        assertEquals(expectedCreatedAt, dto.getCreatedAt());
        assertEquals(expectedUpdatedAt, dto.getUpdatedAt());
        assertNull(dto.getFunction());
    }

    @Test
    void testFromResultSet_ThrowsSQLException() throws SQLException {
        when(mockResultSet.getLong("id")).thenThrow(new SQLException("Database error"));

        assertThrows(SQLException.class, () -> TabulatedFunctionDTO.fromResultSet(mockResultSet));
    }

    @Test
    void testFromResultSet_DeserializationError() throws SQLException {
        byte[] invalidBytes = "invalid data".getBytes();

        when(mockResultSet.getLong("id")).thenReturn(1L);
        when(mockResultSet.getLong("owner_id")).thenReturn(1L);
        when(mockResultSet.getLong("function_type_id")).thenReturn(1L);
        when(mockResultSet.getString("name")).thenReturn("Invalid Function");
        when(mockResultSet.getBytes("serialized_data")).thenReturn(invalidBytes);
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
        TabulatedFunctionDTO dto4 = new TabulatedFunctionDTO(1L, 5L, 3L, createTestFunction(), "Function1", now, now);
        TabulatedFunctionDTO dto5 = new TabulatedFunctionDTO(1L, 2L, 6L, createTestFunction(), "Function1", now, now);
        TabulatedFunctionDTO dto6 = new TabulatedFunctionDTO(1L, 2L, 3L, createTestFunction(), "Function2", now, now);

        assertEquals(dto1, dto2);
        assertNotEquals(dto1, dto3);
        assertNotEquals(dto1, dto4);
        assertNotEquals(dto1, dto5);
        assertNotEquals(dto1, dto6);

        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1.hashCode(), dto3.hashCode());
    }

    @Test
    void testEqualsWithNull() {
        TabulatedFunctionDTO dto = new TabulatedFunctionDTO(1L, 1L, 1L, createTestFunction(), "Test",
                LocalDateTime.now(), LocalDateTime.now());

        assertNotEquals(null, dto);
        assertNotEquals(dto, new Object());
    }

    @Test
    void testEqualsSameObject() {
        TabulatedFunctionDTO dto = new TabulatedFunctionDTO(1L, 1L, 1L, createTestFunction(), "Test",
                LocalDateTime.now(), LocalDateTime.now());

        assertEquals(dto, dto);
    }

    @Test
    void testToString() {
        Long id = 123L;
        Long ownerId = 456L;
        Long functionTypeId = 789L;
        String name = "Test Function";
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 15, 9, 30);
        LocalDateTime updatedAt = LocalDateTime.of(2024, 1, 20, 14, 45);

        TabulatedFunctionDTO dto = new TabulatedFunctionDTO(id, ownerId, functionTypeId, createTestFunction(),
                name, createdAt, updatedAt);

        String result = dto.toString();

        assertNotNull(result);
        assertTrue(result.contains("TabulatedFunctionDTO{"));
        assertTrue(result.contains("id=" + id));
        assertTrue(result.contains("ownerId=" + ownerId));
        assertTrue(result.contains("functionTypeId=" + functionTypeId));
        assertTrue(result.contains("name='" + name + "'"));
        assertTrue(result.contains("createdAt=" + createdAt));
        assertTrue(result.contains("updatedAt=" + updatedAt));
        assertTrue(result.contains("serializedDataLength="));
    }

    @Test
    void testToStringWithNullValues() {
        TabulatedFunctionDTO dto = new TabulatedFunctionDTO(null, null, null, null, null, null, null);

        String result = dto.toString();

        assertNotNull(result);
        assertTrue(result.contains("TabulatedFunctionDTO{"));
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
        TabulatedFunction function = createTestFunction();

        byte[] result = dto.serializeFunction(function);

        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void testSerializeFunctionWithNull() {
        TabulatedFunctionDTO dto = new TabulatedFunctionDTO(1L, 1L, 1L, null, "Test",
                LocalDateTime.now(), LocalDateTime.now());

        byte[] result = dto.serializeFunction(null);

        assertNull(result);
    }

    @Test
    void testSerializeFunctionWithDifferentFunctionType() {
        TabulatedFunctionDTO dto = new TabulatedFunctionDTO(1L, 1L, 1L, null, "Test",
                LocalDateTime.now(), LocalDateTime.now());

        double[] xValues = {1.0, 2.0, 3.0, 4.0};
        double[] yValues = {1.0, 8.0, 27.0, 64.0};
        TabulatedFunction differentFunction = new ArrayTabulatedFunction(xValues, yValues);

        byte[] result = dto.serializeFunction(differentFunction);

        assertNotNull(result);
        assertTrue(result.length > 0);

        byte[] standardResult = dto.serializeFunction(createTestFunction());
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
        TabulatedFunction result = TabulatedFunctionDTO.deserializeFunction(null);

        assertNull(result);
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
    void testEmptyAndSpecialCharactersInName() {
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