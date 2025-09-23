package org.example;

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

public class Main {
    public static void main(String[] args) {
        //TIP Press <shortcut actionId="ShowIntentionActions"/> with your caret at the highlighted text
        // to see how IntelliJ IDEA suggests fixing it.
        System.out.printf("Hello and welcome!%n");

        for (int i = 1; i <= 5; i++) {
            //TIP Press <shortcut actionId="Debug"/> to start debugging your code. We have set one <icon src="AllIcons.Debugger.Db_set_breakpoint"/> breakpoint
            // for you, but you can always add more by pressing <shortcut actionId="ToggleLineBreakpoint"/>.
            System.out.println("i = " + i);
        }
    }
}