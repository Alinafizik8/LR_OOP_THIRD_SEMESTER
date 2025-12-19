package functions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UnitFunctionTest {
    @Test
    void testApply_returnsOneForAnyInput() {
        UnitFunction unit = new UnitFunction();

        //Проверяем разные значения аргумента
        assertEquals(1, unit.apply(0.0), "should return 1");
        assertEquals(1, unit.apply(1.0), "should return 1");
        assertEquals(1, unit.apply(-5.5), "should return 1");
    }

    @Test
    void testConstructor_doesNotThrow() {
        assertDoesNotThrow(UnitFunction::new, "Constructor should not throw any exception");
    }
}
