package Functions;

// Интерфейс MathFunction
interface MathFunction {
    double apply(double x);
}

class IdentityFunction implements MathFunction {
    @Override
    public double apply(double x) {
        return x;
    }
}

// Класс для решения дифференциальных уравнений методом Рунге-Кутты
class RungeKuttaFunction implements MathFunction {

    private final double x0;        // начальное значение x
    private final double y0;        // начальное значение y
    private final MathFunction2D f; // функция dy/dx = f(x, y)
    private final double stepSize;  // шаг интегрирования

    // Конструктор
    public RungeKuttaFunction(double x0, double y0, MathFunction2D f, double stepSize) {
        this.x0 = x0;
        this.y0 = y0;
        this.f = f;
        this.stepSize = stepSize;
    }

    @Override
    public double apply(double xTarget) {
        if (xTarget == x0) return y0;

        double x = x0;
        double y = y0;
        double h = stepSize;

        // Определяем направление интегрирования
        if (xTarget < x0) h = -h;

        while (Math.abs(x - xTarget) > 1e-10) {
            // Если следующий шаг выходит за пределы xTarget — корректируем шаг
            if ((x < xTarget && x + h > xTarget) || (x > xTarget && x + h < xTarget)) {
                h = xTarget - x;
            }

            // Метод Рунге-Кутты 4-го порядка
            double k1 = f.apply(x, y);
            double k2 = f.apply(x + h/2, y + h*k1/2);
            double k3 = f.apply(x + h/2, y + h*k2/2);
            double k4 = f.apply(x + h, y + h*k3);

            y = y + h * (k1 + 2*k2 + 2*k3 + k4) / 6;
            x = x + h;

            // Защита от бесконечного цикла
            if (Math.abs(h) < 1e-14) break;
        }

        return y;
    }

    // Вспомогательный интерфейс для функции двух переменных
    @FunctionalInterface
    public interface MathFunction2D {
        double apply(double x, double y);
    }
}

class CompositeFunction implements MathFunction {

    private final MathFunction firstFunction;   // f(x)
    private final MathFunction secondFunction;  // g(x)

    /**
     * Конструктор композиции: h(x) = g(f(x))
     *
     * @param firstFunction  функция f, применяемая первой
     * @param secondFunction функция g, применяемая второй
     */
    public CompositeFunction(MathFunction firstFunction, MathFunction secondFunction) {
        if (firstFunction == null || secondFunction == null) {
            throw new IllegalArgumentException("Функции не могут быть null");
        }
        this.firstFunction = firstFunction;
        this.secondFunction = secondFunction;
    }

    @Override
    public double apply(double x) {
        double intermediate = firstFunction.apply(x);
        return secondFunction.apply(intermediate);
    }
}

// f(x) = x^2
class SquareFunction implements MathFunction {
    @Override
    public double apply(double x) {
        return x * x;
    }
}

// f(x) = x + 1
class AddOneFunction implements MathFunction {
    @Override
    public double apply(double x) {
        return x + 1;
    }
}

// f(x) = 2 * x
class DoubleFunction implements MathFunction {
    @Override
    public double apply(double x) {
        return 2 * x;
    }
}

public class Task1 {
    public static void main(String[] args) {
        System.out.printf("Hello and welcome!%n");

        for (int i = 1; i <= 5; i++) {
            System.out.println("i = " + i);
        }
    }
}