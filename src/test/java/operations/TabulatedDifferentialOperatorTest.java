package operations;

import functions.*;
import functions.factory.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TabulatedDifferentialOperatorTest {

    // <<<<<>>>>> Конструкторы и геттер/сеттер

    @Test
    void defaultConstructor_usesArrayTabulatedFunctionFactory() {
        TabulatedDifferentialOperator op = new TabulatedDifferentialOperator();
        assertTrue(op.getFactory() instanceof ArrayTabulatedFunctionFactory);
    }

    @Test
    void parameterizedConstructor_usesProvidedFactory() {
        TabulatedFunctionFactory factory = new LinkedListTabulatedFunctionFactory();
        TabulatedDifferentialOperator op = new TabulatedDifferentialOperator(factory);
        assertEquals(factory, op.getFactory());
    }

    @Test
    void setFactory_updatesFactory() {
        TabulatedDifferentialOperator op = new TabulatedDifferentialOperator();
        TabulatedFunctionFactory newFactory = new LinkedListTabulatedFunctionFactory();
        op.setFactory(newFactory);
        assertEquals(newFactory, op.getFactory());
    }

    // <<<<<>>>>> Исключения

    @Test
    void derive_nullFunction_throwsIllegalArgumentException() {
        TabulatedDifferentialOperator op = new TabulatedDifferentialOperator();
        assertThrows(IllegalArgumentException.class, () -> op.derive(null));
    }

    // <<<<<>>>>> Численное дифференцирование

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
    void derive_quadraticFunction_onUniformGrid() {
        // f(x) = x^2 на [0, 2] с шагом 0.5 -> x = [0, 0.5, 1.0, 1.5, 2.0]
        double[] x = {0.0, 0.5, 1.0, 1.5, 2.0};
        double[] y = {0.0, 0.25, 1.0, 2.25, 4.0};

        TabulatedFunction f = new ArrayTabulatedFunction(x, y);
        TabulatedDifferentialOperator op = new TabulatedDifferentialOperator();
        TabulatedFunction df = op.derive(f);

        // Ожидаем: f'(x) = [0.5, 1.0, 2.0, 3.0, 3.5]
        assertEquals(0.5, df.getY(0), 1e-10);  // (0.25 - 0.0) / 0.5 = 0.5
        assertEquals(1.0, df.getY(1), 1e-10);  // (1.0 - 0.0) / (1.0 - 0.0) = 1.0
        assertEquals(2.0, df.getY(2), 1e-10);  // (2.25 - 0.25) / (1.5 - 0.5) = 2.0
        assertEquals(3.0, df.getY(3), 1e-10);  // (4.0 - 1.0) / (2.0 - 1.0) = 3.0
        assertEquals(3.5, df.getY(4), 1e-10);  // (4.0 - 2.25) / 0.5 = 3.5
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

    // <<<<<>>>>> Крайние случаи

    @Test
    void derive_twoPoints_onlyFirstAndLast() {
        TabulatedFunction f = new ArrayTabulatedFunction(
                new double[]{0.0, 2.0},
                new double[]{0.0, 4.0}
        );

        TabulatedDifferentialOperator op = new TabulatedDifferentialOperator();
        TabulatedFunction df = op.derive(f);

        assertEquals(2, df.getCount());
        assertEquals(2.0, df.getY(0), 1e-10); // (4-0)/(2-0) = 2 — первая точка
        assertEquals(2.0, df.getY(1), 1e-10); // (4-0)/(2-0) = 2 — последняя точка
    }
}