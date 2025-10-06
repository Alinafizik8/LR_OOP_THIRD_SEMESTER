package operations;

import functions.*;
import functions.factory.ArrayTabulatedFunctionFactory;
import functions.factory.LinkedListTabulatedFunctionFactory;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TabulatedDifferentialOperatorTest {

    @Test
    void derive_linearFunction_returnsConstantDerivative() {
        // f(x) = 2x -> f'(x) = 2
        TabulatedFunction f = new ArrayTabulatedFunction(
                new double[]{0.0, 1.0, 2.0, 3.0},
                new double[]{0.0, 2.0, 4.0, 6.0}
        );

        TabulatedDifferentialOperator op = new TabulatedDifferentialOperator();
        TabulatedFunction df = op.derive(f);

        assertEquals(4, df.getCount());
        assertEquals(2.0, df.getY(0), 1e-10);
        assertEquals(2.0, df.getY(1), 1e-10);
        assertEquals(2.0, df.getY(2), 1e-10);
        assertEquals(2.0, df.getY(3), 1e-10);
    }

    @Test
    void derive_quadraticFunction_returnsLinearDerivative() {
        // f(x) = x^2 -> f'(x) = 2x
        TabulatedFunction f = new LinkedListTabulatedFunction(
                new double[]{0.0, 1.0, 2.0, 3.0},
                new double[]{0.0, 1.0, 4.0, 9.0}
        );

        TabulatedDifferentialOperator op = new TabulatedDifferentialOperator(
                new LinkedListTabulatedFunctionFactory()
        );
        TabulatedFunction df = op.derive(f);

        assertEquals(4, df.getCount());
        assertEquals(1.0, df.getY(0), 1e-10);   // (1-0)/(1-0) = 1
        assertEquals(2.0, df.getY(1), 1e-10);   // (4-0)/(2-0) = 2
        assertEquals(4.0, df.getY(2), 1e-10);   // (9-1)/(3-1) = 4
        assertEquals(5.0, df.getY(3), 1e-10);   // (9-4)/(3-2) = 5 но при центральной: (9-1)/(3-1)=4
    }

    // Уточнённый тест с равномерной сеткой и центральной разностью
    @Test
    void derive_xSquared_onUniformGrid() {
        // f(x) = x^2 на [0, 2] с шагом 0.5 -> x = [0, 0.5, 1.0, 1.5, 2.0]
        double[] x = {0.0, 0.5, 1.0, 1.5, 2.0};
        double[] y = {0.0, 0.25, 1.0, 2.25, 4.0};

        TabulatedFunction f = new ArrayTabulatedFunction(x, y);
        TabulatedDifferentialOperator op = new TabulatedDifferentialOperator();
        TabulatedFunction df = op.derive(f);

        // Ожидаем: f'(x) ≈ [0.5, 1.0, 2.0, 3.0, 3.5]
        assertEquals(0.5, df.getY(0), 1e-10);  // (0.25 - 0.0) / 0.5 = 0.5
        assertEquals(1.0, df.getY(1), 1e-10);  // (1.0 - 0.0) / (1.0 - 0.0) = 1.0
        assertEquals(2.0, df.getY(2), 1e-10);  // (2.25 - 0.25) / (1.5 - 0.5) = 2.0
        assertEquals(3.0, df.getY(3), 1e-10);  // (4.0 - 1.0) / (2.0 - 1.0) = 3.0
        assertEquals(3.5, df.getY(4), 1e-10);  // (4.0 - 2.25) / 0.5 = 3.5
    }

    @Test
    void derive_withLinkedListFactory_returnsLinkedListTabulatedFunction() {
        TabulatedFunction f = new ArrayTabulatedFunction(
                new double[]{0.0, 1.0},
                new double[]{0.0, 1.0}
        );

        TabulatedDifferentialOperator op = new TabulatedDifferentialOperator(
                new LinkedListTabulatedFunctionFactory()
        );
        TabulatedFunction df = op.derive(f);

        assertTrue(df instanceof LinkedListTabulatedFunction);
        assertEquals(1.0, df.getY(0), 1e-10);
        assertEquals(1.0, df.getY(1), 1e-10);
    }

    @Test
    void derive_withArrayFactory_returnsArrayTabulatedFunction() {
        TabulatedFunction f = new LinkedListTabulatedFunction(
                new double[]{0.0, 1.0},
                new double[]{0.0, 1.0}
        );

        TabulatedDifferentialOperator op = new TabulatedDifferentialOperator(
                new ArrayTabulatedFunctionFactory()
        );
        TabulatedFunction df = op.derive(f);

        assertTrue(df instanceof ArrayTabulatedFunction);
        assertEquals(1.0, df.getY(0), 1e-10);
        assertEquals(1.0, df.getY(1), 1e-10);
    }
}