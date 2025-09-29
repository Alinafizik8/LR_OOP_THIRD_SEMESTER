package functions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RungeKuttaFunctionTest {

    // Точное решение для dy/dx = x + y, y(0) = 1 -> y(x) = 2*exp(x) - x - 1
    private static double exactSolution(double x) {
        return 2 * Math.exp(x) - x - 1;
    }

    // Правая часть ОДУ: dy/dx = x + y
    private static final RungeKuttaFunction.MathFunction2D ODE_X_PLUS_Y = (x, y) -> x + y;

    // Правая часть ОДУ: dy/dx = -y
    private static final RungeKuttaFunction.MathFunction2D ODE_MINUS_Y = (x, y) -> -y;

    // <<<<>>>> 1. Тест: xTarget == x0 -> возврат y0
    @Test
    void testApply_WhenXEqualsX0_ReturnsY0() {
        RungeKuttaFunction rk = new RungeKuttaFunction(1.5, 3.7, ODE_X_PLUS_Y, 0.1);
        assertEquals(3.7, rk.apply(1.5), 1e-12);
    }

    // <<<<>>>> 2. Тест: интегрирование вперёд (обычный шаг)
    @Test
    void testApply_ForwardIntegration_NormalStep() {
        RungeKuttaFunction rk = new RungeKuttaFunction(0.0, 1.0, ODE_X_PLUS_Y, 0.1);
        double result = rk.apply(0.2); // два шага по 0.1
        double expected = exactSolution(0.2);
        assertEquals(expected, result, 1e-6);
    }

    // <<<<>>>> 3. Тест: интегрирование назад
    @Test
    void testApply_BackwardIntegration() {
        double yAt1 = exactSolution(1.0);
        RungeKuttaFunction rk = new RungeKuttaFunction(1.0, yAt1, ODE_X_PLUS_Y, 0.1);
        double result = rk.apply(0.0);
        assertEquals(1.0, result, 1e-5);
    }

    // <<<<>>>> 4. Тест: коррекция последнего шага (когда h выходит за xTarget)
    @Test
    void testApply_StepCorrectionAtEnd() {
        // Шаг 0.3, но цель — 0.5 → последний шаг = 0.2
        RungeKuttaFunction rk = new RungeKuttaFunction(0.0, 1.0, ODE_X_PLUS_Y, 0.3);
        double result = rk.apply(0.5);
        double expected = exactSolution(0.5);
        assertEquals(expected, result, 1e-4);
    }

    // <<<<>>>> 5. Тест: очень маленький шаг -> срабатывает break (защита от зацикливания)
    @Test
    void testApply_VerySmallStepSize_TriggerBreakCondition() {
        // Шаг такой маленький, что после коррекции |h| < 1e-14
        RungeKuttaFunction rk = new RungeKuttaFunction(0.0, 1.0, ODE_X_PLUS_Y, 1e-15);
        // Должен завершиться без зависания
        assertDoesNotThrow(() -> {
            double result = rk.apply(1e-13); // цель чуть больше шага
            // Результат может быть неточным, но не NaN/Inf
            assertFalse(Double.isNaN(result));
            assertFalse(Double.isInfinite(result));
        });
    }

    // <<<<>>>> 6. Тест: нулевой шаг (крайний случай)
    @Test
    void testApply_ZeroStepSize_DoesNotHang() {
        RungeKuttaFunction rk = new RungeKuttaFunction(0.0, 1.0, ODE_X_PLUS_Y, 0.0);
        assertDoesNotThrow(() -> {
            double result = rk.apply(0.1);
            // После первого шага h = 0.1 (из-за xTarget > x0), но в цикле:
            // h = xTarget - x = 0.1 → нормальный шаг
            // Однако если stepSize = 0 и xTarget != x0, то h = ±0 → потом h = xTarget - x
            // Всё равно не зависает
        });
    }

    // <<<<>>>> 7. Тест: другое ОДУ (dy/dx = -y → y = exp(-x))
    @Test
    void testApply_WithDifferentODE() {
        RungeKuttaFunction rk = new RungeKuttaFunction(0.0, 1.0, ODE_MINUS_Y, 0.01);
        double result = rk.apply(1.0);
        double expected = Math.exp(-1.0);
        assertEquals(expected, result, 1e-6);
    }

    // <<<<>>>> 8. Тест: большие значения x (проверка устойчивости) <<<<>>>>
    @Test
    void testApply_LargeXValue_NoException() {
        RungeKuttaFunction rk = new RungeKuttaFunction(0.0, 1.0, ODE_X_PLUS_Y, 0.1);
        assertDoesNotThrow(() -> {
            double result = rk.apply(10.0);
            assertTrue(Double.isFinite(result));
        });
    }

    // <<<<>>>> 9. Тест: отрицательные x и y <<<<>>>>
    @Test
    void testApply_NegativeValues() {
        // dy/dx = -y, y(0) = -1 → y(x) = -exp(-x)
        RungeKuttaFunction rk = new RungeKuttaFunction(0.0, -1.0, ODE_MINUS_Y, 0.01);
        double result = rk.apply(-1.0); // интегрируем назад
        double expected = -Math.exp(1.0); // y(-1) = -e
        assertEquals(expected, result, 1e-5);
    }

    // <<<<>>>> 10. Тест: шаг больше расстояния до цели (один шаг с коррекцией) <<<<>>>>
    @Test
    void testApply_StepLargerThanDistance_SingleCorrectedStep() {
        RungeKuttaFunction rk = new RungeKuttaFunction(0.0, 1.0, ODE_X_PLUS_Y, 10.0);
        double result = rk.apply(0.5); // шаг 10, но цель 0.5 → h = 0.5
        double expected = exactSolution(0.5);
        assertEquals(expected, result, 1e-3);
    }
}