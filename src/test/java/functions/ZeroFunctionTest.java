package functions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ZeroFunctionTest {
    @Test
    void testApply_returnsZeroForAnyInput() {
        ZeroFunction zero = new ZeroFunction();

        //Проверяем разные значения аргумента
        assertEquals(0, zero.apply(0.0), "should return 0");
        assertEquals(0, zero.apply(1.0), "should return 0");
        assertEquals(0, zero.apply(-5.5), "should return 0");
    }

    @Test
    void testConstructor_doesNotThrow() {
        assertDoesNotThrow(UnitFunction::new, "Constructor should not throw any exception");
    }
}