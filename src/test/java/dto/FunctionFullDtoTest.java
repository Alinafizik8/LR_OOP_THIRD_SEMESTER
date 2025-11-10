package dto;

import functions.ArrayTabulatedFunction;
import functions.TabulatedFunction;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class FunctionFullDtoTest {

    @Test
    void testFromTabulatedFunction() {
        double[] originalX = {0.0, 1.0, 2.0};
        double[] originalY = {0.0, 1.0, 4.0};
        TabulatedFunction originalFunc = new ArrayTabulatedFunction(originalX, originalY);
        Long id = 100L;
        String name = "TestFunc";
        String typeName = "ArrayTabulatedFunction";
        LocalDateTime createdAt = LocalDateTime.now();

        FunctionFullDto fullDto = FunctionFullDto.fromTabulatedFunction(originalFunc, id, name, typeName, createdAt);

        assertNotNull(fullDto);
        assertEquals(id, fullDto.getId());
        assertEquals(name, fullDto.getName());
        assertEquals(typeName, fullDto.getTypeName());
        assertEquals(createdAt, fullDto.getCreatedAt());

        double[] dtoX = fullDto.getXValues();
        double[] dtoY = fullDto.getYValues();
        assertArrayEquals(originalX, dtoX);
        assertArrayEquals(originalY, dtoY);

        originalX[0] = -999.0;
        originalY[0] = -999.0;

        assertNotEquals(-999.0, fullDto.getXValues()[0]);
        assertNotEquals(-999.0, fullDto.getYValues()[0]);
        assertEquals(0.0, fullDto.getXValues()[0], 0.0001);
        assertEquals(0.0, fullDto.getYValues()[0], 0.0001);
    }

    @Test
    void testConstructorAndGetters() {
        Long id = 1L;
        String name = "MyFunc";
        String typeName = "CustomFunction";
        double[] xValues = {1.0, 2.0};
        double[] yValues = {10.0, 20.0};
        LocalDateTime createdAt = LocalDateTime.of(2023, 12, 25, 10, 30);

        double originalFirstX = xValues[0];
        double originalFirstY = yValues[0];

        FunctionFullDto dto = new FunctionFullDto(id, name, typeName, xValues, yValues, createdAt);

        assertEquals(id, dto.getId());
        assertEquals(name, dto.getName());
        assertEquals(typeName, dto.getTypeName());
        assertArrayEquals(xValues, dto.getXValues());
        assertArrayEquals(yValues, dto.getYValues());
        assertEquals(createdAt, dto.getCreatedAt());

        xValues[0] = -1.0;
        yValues[0] = -1.0;

        assertNotEquals(-1.0, dto.getXValues()[0], 0.0001);
        assertNotEquals(-1.0, dto.getYValues()[0], 0.0001);
        assertEquals(originalFirstX, dto.getXValues()[0], 0.0001); // 1.0
        assertEquals(originalFirstY, dto.getYValues()[0], 0.0001); // 10.0

        double[] dtoXClone = dto.getXValues();
        double[] dtoYClone = dto.getYValues();
        dtoXClone[0] = -2.0;
        dtoYClone[0] = -2.0;

        assertNotEquals(-2.0, dto.getXValues()[0], 0.0001);
        assertNotEquals(-2.0, dto.getYValues()[0], 0.0001);
        assertEquals(originalFirstX, dto.getXValues()[0], 0.0001); // 1.0
        assertEquals(originalFirstY, dto.getYValues()[0], 0.0001); // 10.0
    }

    @Test
    void testConstructorWithNullArrays() {
        Long id = 2L;
        String name = "NullArrayFunc";
        String typeName = "NullFunction";
        double[] xValues = null;
        double[] yValues = null;
        LocalDateTime createdAt = LocalDateTime.of(2024, 6, 15, 14, 45);

        FunctionFullDto dto = new FunctionFullDto(id, name, typeName, xValues, yValues, createdAt);

        assertEquals(id, dto.getId());
        assertEquals(name, dto.getName());
        assertEquals(typeName, dto.getTypeName());
        assertNull(dto.getXValues());
        assertNull(dto.getYValues());
        assertEquals(createdAt, dto.getCreatedAt());
    }

    @Test
    void testDefaultConstructor() {
        FunctionFullDto dto = new FunctionFullDto();

        assertNull(dto.getId());
        assertNull(dto.getName());
        assertNull(dto.getTypeName());
        assertNull(dto.getXValues());
        assertNull(dto.getYValues());
        assertNull(dto.getCreatedAt());
    }
}