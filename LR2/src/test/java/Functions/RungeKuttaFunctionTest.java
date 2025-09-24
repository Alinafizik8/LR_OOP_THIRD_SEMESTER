package Functions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RungeKuttaFunctionTest {

    // Точное решение для dy/dx = x + y, y(0)=1: y(x) = 2*exp(x) - x - 1
    private static double exactSolution(double x) {
        return 2 * Math.exp(x) - x - 1;
    }

    // Правая часть ОДУ
    private static final RungeKuttaFunction.MathFunction2D ODE = (x, y) -> x + y;

    // Погрешность для сравнения (для шага 0.01)
    private static final double TOLERANCE_001 = 1e-5;
    private static final double TOLERANCE_0001 = 1e-7;

    @Test
    void testApply_AtInitialPoint_ReturnsInitialValue() {
        RungeKuttaFunction rk = new RungeKuttaFunction(0.0, 1.0, ODE, 0.01);
        assertEquals(1.0, rk.apply(0.0), 1e-12);
    }

    @Test
    void testApply_ForwardIntegration_Step0_01() {
        RungeKuttaFunction rk = new RungeKuttaFunction(0.0, 1.0, ODE, 0.01);

        double x = 1.0;
        double expected = exactSolution(x);
        double actual = rk.apply(x);

        assertEquals(expected, actual, TOLERANCE_001,
                "Значение в точке x=1.0 с шагом 0.01 должно быть близко к точному решению");
    }

    @Test
    void testApply_ForwardIntegration_Step0_001() {
        RungeKuttaFunction rk = new RungeKuttaFunction(0.0, 1.0, ODE, 0.001);

        double x = 1.0;
        double expected = exactSolution(x);
        double actual = rk.apply(x);

        assertEquals(expected, actual, TOLERANCE_0001,
                "Значение в точке x=1.0 с шагом 0.001 должно быть точнее");
    }

    @Test
    void testApply_BackwardIntegration() {
        RungeKuttaFunction rk = new RungeKuttaFunction(1.0, exactSolution(1.0), ODE, 0.01);

        double x = 0.0;
        double expected = 1.0;
        double actual = rk.apply(x);

        assertEquals(expected, actual, TOLERANCE_001,
                "Интегрирование назад от x=1 к x=0 должно вернуть начальное значение");
    }

    @Test
    void testApply_MultipleCalls_ConsistentResults() {
        RungeKuttaFunction rk = new RungeKuttaFunction(0.0, 1.0, ODE, 0.01);

        double result1 = rk.apply(0.5);
        double result2 = rk.apply(0.5);

        assertEquals(result1, result2, 1e-12,
                "Повторные вызовы для одного x должны возвращать одинаковый результат");
    }

    @Test
    void testApply_WithVerySmallStep() {
        RungeKuttaFunction rk = new RungeKuttaFunction(0.0, 1.0, ODE, 1e-6);

        double x = 0.1;
        double expected = exactSolution(x);
        double actual = rk.apply(x);

        assertEquals(expected, actual, 1e-8,
                "Очень маленький шаг должен давать высокую точность");
    }

    @Test
    void testApply_WithLargeStep_LessAccurate() {
        RungeKuttaFunction rkCoarse = new RungeKuttaFunction(0.0, 1.0, ODE, 0.5);
        RungeKuttaFunction rkFine = new RungeKuttaFunction(0.0, 1.0, ODE, 0.01);

        double x = 2.0;
        double coarse = rkCoarse.apply(x);
        double fine = rkFine.apply(x);
        double exact = exactSolution(x);

        assertTrue(Math.abs(coarse - exact) > Math.abs(fine - exact),
                "Крупный шаг должен быть менее точным, чем мелкий");
    }

    @Test
    void testApply_ExtremeValues_NoExceptions() {
        RungeKuttaFunction rk = new RungeKuttaFunction(0.0, 1.0, ODE, 0.01);

        assertDoesNotThrow(() -> rk.apply(10.0));
        assertDoesNotThrow(() -> rk.apply(-5.0));
    }

    @Test
    void testApply_ZeroStepSize_ShouldNotCrash() {
        RungeKuttaFunction rk = new RungeKuttaFunction(0.0, 1.0, ODE, 0.0);

        // Ожидаем, что не упадёт, но результат может быть некорректным
        assertDoesNotThrow(() -> rk.apply(1.0));
    }

    @Test
    void testApply_WithDifferentODE() {
        // dy/dx = -y, y(0)=1 → y(x) = exp(-x)
        RungeKuttaFunction.MathFunction2D decay = (x, y) -> -y;
        RungeKuttaFunction rk = new RungeKuttaFunction(0.0, 1.0, decay, 0.01);

        double x = 1.0;
        double expected = Math.exp(-x);
        double actual = rk.apply(x);

        assertEquals(expected, actual, 1e-5,
                "Должно корректно работать с другим ОДУ: dy/dx = -y");
    }
}