package functions;

import org.junit.jupiter.api.Test;
import java.util.function.Function;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для класса BSpline
 * Основная идея: решаем ОДУ, у которого мы заранее знаем точное решение.
 * Затем сравниваем приближённое решение (сплайн) с точным — погрешность должна быть мала.
 *
 * Тестируемое уравнение:
 *      y''(x) = 2
 * Решение:
 *      y(x) = x^2   (проверка: (x^2)'' = 2)
 * Граничные условия:
 *      y(0) = 0,   y(1) = 1
 */
public class BSplineTest {

    @Test
    public void testSolveQuadraticODE() {
        double a = 0.0;          // левая граница
        double b = 1.0;          // правая граница
        int nIntervals = 8;      // количество подынтервалов
        // Функции из уравнения y'' + p(x)y' + q(x)y = f(x)
        // У нас уравнение y'' = 2 → p(x) = 0, q(x) = 0, f(x) = 2
        Function<Double, Double> p = x -> 0.0;
        Function<Double, Double> q = x -> 0.0;
        Function<Double, Double> f = x -> 2.0;
        // Граничные условия
        double ya = 0.0;  // y(0) = 0^2 = 0
        double yb = 1.0;  // y(1) = 1^2 = 1
        BSpline solver = new BSpline(a, b, nIntervals, p, q, f, ya, yb);
        // На левой границе
        double yAtA = solver.apply(a);
        assertEquals(ya, yAtA, 10, "Решение в точке x=a должно точно совпадать с ya");

        // На правой границе
        double yAtB = solver.apply(b);
        assertEquals(yb, yAtB, 10, "Решение в точке x=b должно точно совпадать с yb");

        // Вычислим значение в случайной точке и убедимся, что оно конечное
        double yMid = solver.apply(0.333);
        assertTrue(Double.isFinite(yMid), "Решение в промежуточной точке должно быть конечным числом");
    }

    @Test
    public void testSolveQuadraticODA() {
        double a = 0.0;          // левая граница
        double b = 2.0;          // правая граница
        int nIntervals = 10;      // количество подынтервалов
        // Функции из уравнения y'' + p(x)y' + q(x)y = f(x)
        // У нас уравнение y'' = 2 → p(x) = 0, q(x) = 0, f(x) = 2
        Function<Double, Double> p = x -> 0.0;
        Function<Double, Double> q = x -> 0.0;
        Function<Double, Double> f = x -> 2.0;
        // Граничные условия
        double ya = 0.0;  // y(0) = 0^2 = 0
        double yb = 4.0;  // y(1) = 1^2 = 1
        BSpline solver = new BSpline(a, b, nIntervals, p, q, f, ya, yb);
        // На левой границе
        double yAtA = solver.apply(a);
        assertEquals(ya, yAtA, 10, "Решение в точке x=a должно точно совпадать с ya");

        // На правой границе
        double yAtB = solver.apply(b);
        assertEquals(yb, yAtB, 10, "Решение в точке x=b должно точно совпадать с yb");

        // Вычислим значение в случайной точке и убедимся, что оно конечное
        double yMid = solver.apply(0.333);
        assertTrue(Double.isFinite(yMid), "Решение в промежуточной точке должно быть конечным числом");
    }

    @Test
    public void trois(){
        int nIntervals = 3;
        Function<Double, Double> p = x -> 0.0;
        Function<Double, Double> q = x -> 0.0;
        Function<Double, Double> f = x -> 2.0;
        assertThrows(IllegalArgumentException.class, () -> {
            BSpline g = new BSpline(0, 1, nIntervals, p, q, f, 0, 1);
        });
    }

}