package functions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConstantFunctionTest {
    private ConstantFunction digit;

    //выполнится перед каждым тестом
    @BeforeEach
    void setUp() {
        digit = new ConstantFunction(5);
    }

    @Test
    void getT() {
        assertEquals(5,digit.getT(),"should return 5");

    }

    @Test
    void apply() {
        assertEquals(5,digit.apply(2),"still should return 5");
    }
}