package operations;
import functions.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LeftSteppingDifferentialOperatorTest {

    private static final double DELTA = 1e-2;

    @Test
    void testDerive_SqrFunction_WithSmallStep() {
        MathFunction sqr = new SqrFunction();
        double step = 1e-5;
        LeftSteppingDifferentialOperator operator = new LeftSteppingDifferentialOperator(step);
        MathFunction derivative = operator.derive(sqr);

        // Тестируем в нескольких точках
        assertEquals(0.0, derivative.apply(0.0), DELTA);      // f'(0) = 2*0 = 0
        assertEquals(2.0, derivative.apply(1.0), DELTA);      // f'(1) = 2*1 = 2
        assertEquals(6.0, derivative.apply(3.0), DELTA);      // f'(3) = 2*3 = 6
        assertEquals(-4.0, derivative.apply(-2.0), DELTA);    // f'(-2) = 2*(-2) = -4
    }

    @Test
    void testDerive_LinearFunction_ExactResult() {
        double[] x = {0.0,1.0,2.0,3.0};
        double[] y = {5.0,8.0,11.0,14.0};
        MathFunction linear = new ArrayTabulatedFunction(x,y); // f(x) = 3x + 5
        double step = 0.1;
        LeftSteppingDifferentialOperator operator = new LeftSteppingDifferentialOperator(step);
        MathFunction derivative = operator.derive(linear);

        // Для линейной функции левая разностная производная даёт ТОЧНОЕ значение!
        assertEquals(3.0, derivative.apply(-10.0), 1e-12);
        assertEquals(3.0, derivative.apply(0.0), 1e-12);
        assertEquals(3.0, derivative.apply(100.0), 1e-12);
    }

    @Test
    void testDerive_ConstantFunction_ZeroDerivative() {
        MathFunction constant = x -> 42.0;
        double step = 0.01;
        LeftSteppingDifferentialOperator operator = new LeftSteppingDifferentialOperator(step);
        MathFunction derivative = operator.derive(constant);

        assertEquals(0.0, derivative.apply(-5.0), 1e-12);
        assertEquals(0.0, derivative.apply(0.0), 1e-12);
        assertEquals(0.0, derivative.apply(7.77), 1e-12);
    }

    @Test
    void testConstructor_ThrowsException_OnInvalidStep() {
        // Ноль
        assertThrows(IllegalArgumentException.class, () -> new LeftSteppingDifferentialOperator(0.0));
        // Отрицательный
        assertThrows(IllegalArgumentException.class, () -> new LeftSteppingDifferentialOperator(-0.1));
        // Бесконечность
        assertThrows(IllegalArgumentException.class, () -> new LeftSteppingDifferentialOperator(Double.POSITIVE_INFINITY));
        // NaN
        assertThrows(IllegalArgumentException.class, () -> new LeftSteppingDifferentialOperator(Double.NaN));
    }

    @Test
    void testDerive_WithDifferentSteps_AccuracyImproves() {
        MathFunction sqr = new SqrFunction();
        double x = 2.0; // точная производная = 4.0

        LeftSteppingDifferentialOperator op1 = new LeftSteppingDifferentialOperator(1e-2);
        LeftSteppingDifferentialOperator op2 = new LeftSteppingDifferentialOperator(1e-4);

        double approx1 = op1.derive(sqr).apply(x); // менее точно
        double approx2 = op2.derive(sqr).apply(x); // точнее

        double error1 = Math.abs(approx1 - 4.0);
        double error2 = Math.abs(approx2 - 4.0);

        assertTrue(error2 < error1, "Более мелкий шаг должен давать меньшую погрешность");
    }
}