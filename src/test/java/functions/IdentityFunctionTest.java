package functions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class IdentityFunctionTest {

    @Test
    void apply() {
        IdentityFunction function = new IdentityFunction();

        assertEquals(5.0, function.apply(5.0), 1e-10);
        assertEquals(0.0, function.apply(0.0), 1e-10);
        assertEquals(-3.5, function.apply(-3.5), 1e-10);
        assertEquals(Double.MAX_VALUE, function.apply(Double.MAX_VALUE));
        assertEquals(Double.MIN_VALUE, function.apply(Double.MIN_VALUE));
    }
}