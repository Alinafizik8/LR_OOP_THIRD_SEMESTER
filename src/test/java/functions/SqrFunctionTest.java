package functions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

//x^2
class SqrFunctionTest {
    @Test
    void apply() {
        SqrFunction digit = new SqrFunction();
        assertEquals(16, digit.apply(4),"4^2=16");
    }
}