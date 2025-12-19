package functions.factory;

import functions.ArrayTabulatedFunction;
import functions.LinkedListTabulatedFunction;
import functions.TabulatedFunction;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TabulatedFunctionFactoryTest {

    private static final double[] X = {0.0, 1.0, 2.0};
    private static final double[] Y = {0.0, 1.0, 4.0};

    @Test
    void arrayFactory_createsArrayTabulatedFunction() {
        TabulatedFunctionFactory factory = new ArrayTabulatedFunctionFactory();
        TabulatedFunction f = factory.create(X, Y);
        assertTrue(f instanceof ArrayTabulatedFunction);
        assertEquals(3, f.getCount());
        assertEquals(4.0, f.getY(2), 1e-10);
    }

    @Test
    void linkedListFactory_createsLinkedListTabulatedFunction() {
        TabulatedFunctionFactory factory = new LinkedListTabulatedFunctionFactory();
        TabulatedFunction f = factory.create(X, Y);
        assertTrue(f instanceof LinkedListTabulatedFunction);
        assertEquals(3, f.getCount());
        assertEquals(1.0, f.getX(1), 1e-10);
    }

    @Test
    void factories_produceEquivalentFunctions() {
        TabulatedFunctionFactory arrayFactory = new ArrayTabulatedFunctionFactory();
        TabulatedFunctionFactory linkedFactory = new LinkedListTabulatedFunctionFactory();

        TabulatedFunction f1 = arrayFactory.create(X, Y);
        TabulatedFunction f2 = linkedFactory.create(X, Y);

        assertEquals(f1.getCount(), f2.getCount());
        for (int i = 0; i < f1.getCount(); i++) {
            assertEquals(f1.getX(i), f2.getX(i), 1e-10);
            assertEquals(f1.getY(i), f2.getY(i), 1e-10);
        }
    }
}