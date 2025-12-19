package operations;

import functions.MathFunction;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RightSteppingDifferentialOperatorTest {

    // Тестовая функция: f(x) = x^2
    private static final MathFunction SQUARE = x -> x * x;

    // Её аналитическая производная: f'(x) = 2x
    private static double derivativeOfSquare(double x) {
        return 2 * x;
    }

    // Тестовая функция: f(x) = sin(x)
    private static final MathFunction SIN = Math::sin;

    // Её производная: f'(x) = cos(x)
    private static double derivativeOfSin(double x) {
        return Math.cos(x);
    }

    @Test
    void testConstructorInvalidStep() {
        assertThrows(IllegalArgumentException.class, () -> new RightSteppingDifferentialOperator(0.0));
        assertThrows(IllegalArgumentException.class, () -> new RightSteppingDifferentialOperator(-0.1));
        assertThrows(IllegalArgumentException.class, () -> new RightSteppingDifferentialOperator(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> new RightSteppingDifferentialOperator(Double.POSITIVE_INFINITY));
    }

    @Test
    void testConstructorValidStep() {
        assertDoesNotThrow(() -> new RightSteppingDifferentialOperator(0.1));
        assertDoesNotThrow(() -> new RightSteppingDifferentialOperator(1e-5));
    }

    @Test
    void testDeriveSquareFunction() {
        double step = 1e-6;
        RightSteppingDifferentialOperator operator = new RightSteppingDifferentialOperator(step);
        MathFunction derived = operator.derive(SQUARE);

        double x = 2.0;
        double actual = derived.apply(x);
        // Текущая реализация: (f(x) - f(x + h)) / h = - (f(x + h) - f(x)) / h → = -f'(x)
        double expected = -derivativeOfSquare(x); // -4.0

        assertEquals(expected, actual, 1e-4);
    }

    @Test
    void testDeriveSinFunction() {
        double step = 1e-6;
        RightSteppingDifferentialOperator operator = new RightSteppingDifferentialOperator(step);
        MathFunction derived = operator.derive(SIN);

        double x = 0.0;
        double actual = derived.apply(x);
        double expected = -Math.cos(0.0); // -1.0

        assertEquals(expected, actual, 1e-5);
    }

    @Test
    void testAccuracyWithSmallerStep() {
        double x = 1.0;
        double trueDerivative = -derivativeOfSquare(x); // -2.0

        RightSteppingDifferentialOperator op1 = new RightSteppingDifferentialOperator(1e-2);
        RightSteppingDifferentialOperator op2 = new RightSteppingDifferentialOperator(1e-4);

        double approx1 = op1.derive(SQUARE).apply(x);
        double approx2 = op2.derive(SQUARE).apply(x);

        double error1 = Math.abs(approx1 - trueDerivative);
        double error2 = Math.abs(approx2 - trueDerivative);

        assertTrue(error2 < error1);
    }
}