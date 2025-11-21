package com.example.alina.functions;

import java.util.Arrays;
import java.util.function.Function;
/**
 * Решаем уравнение вида:
 *      y''(x) + p(x) * y'(x) + q(x) * y(x) = f(x),   x ∈ [a, b]
 * с ограничениями:
 *      y(a) = ya,   y(b) = yb.
 * Приближённое решение ищется в виде линейной комбинации кубических B-сплайнов:
 *      y(x) ≈ Σ c_j * B_j(x),
 * где B_j(x) — базисные кубические B-сплайн функции, c_j — коэффициенты.
 */
public class BSpline implements MathFunction {

    // Узловой вектор сплайна: определяет, где "склеиваются" полиномиальные участки
    private final double[] knots;
    // Коэффициенты разложения по базису: y(x) = Σ coefficients[j] * B_j(x)
    private final double[] coefficients;
    // Степень сплайна: 3 = кубический (обеспечивает непрерывность до второй производной)
    private static final int DEGREE = 3;
    /**
     * Конструктор: сразу решает ОДУ и сохраняет коэффициенты сплайна.
     * a - левая граница [a, b]
     * b - правая граница [a, b]
     * nIntervals - количество подынтервалов разбиения [a, b] (минимум 3)
     * p - функция p(x) из уравнения
     * q - функция q(x) из уравнения
     * f - правая часть f(x)
     * ya - значение y(a)
     * yb - значение y(b)
     */
    public BSpline(double a, double b, int nIntervals,
                   Function<Double, Double> p,
                   Function<Double, Double> q,
                   Function<Double, Double> f,
                   double ya, double yb) {
        if (nIntervals < 4) {
            throw new IllegalArgumentException("Интервалов должно быть минимум 4.");
        }
        // Число базисных функций
        int N = nIntervals + 1;

        int numInterior = nIntervals - 3; // потому что m = N + DEGREE + 1 = nIntervals + 5; m = 8 + numInterior => numInterior = nIntervals - 3
        //шаг исходной равномерной сетки на [a,b]
        double h = (b - a) / nIntervals;//was "- 1"!!!
        double[] interior = new double[numInterior];
        for (int i = 0; i < numInterior; i++) {
            interior[i] = a + (i + 1) * h;
        }
        //общая длина узлового вектора
        // Узловой вектор: [a,a,a,a, x1, x2, ..., x_{n-3}, b,b,b,b]
        int totalKnots = N + DEGREE + 1; // = (n+1) + 4 = n+5
        knots = new double[totalKnots];

        //первые 4 элемента в узловом векторе нижняя граница отрезка
        for (int i = 0; i <= DEGREE; i++) knots[i] = a;
        //копируем в середину внутренние узлы
        System.arraycopy(interior, 0, knots, DEGREE + 1, numInterior);
        //последние 4 элемента в узловом векторе верхняя граница отрезка
        for (int i = totalKnots - (DEGREE + 1); i < totalKnots; i++) knots[i] = b;

        // Коллокационные точки — ВНУТРЕННИЕ узлы сетки (без a и b)
        int nColloc = nIntervals - 1;
        double[] colloc = new double[nColloc];
        for (int i = 0; i < nColloc; i++) {
            colloc[i] = a + (i + 0.5) * h;
        }

        // Система уравнений c*A = rhs (где с - искомые коэффициенты)
        double[][] A = new double[N][N];
        double[] rhs = new double[N];

        // Коллокации
        for (int i = 0; i < nColloc; i++) {
            double x = colloc[i];
            double px = p.apply(x);
            double qx = q.apply(x);
            rhs[i] = f.apply(x);
            for (int j = 0; j < N; j++) {
                double b0 = derivBasis(j, DEGREE, x, knots,0);
                double b1 = derivBasis(j, DEGREE, x, knots, 1);
                double b2 = derivBasis(j, DEGREE, x, knots, 2);
                A[i][j] = b2 + px * b1 + qx * b0;
            }
        }

        // Граничные условия
        for (int j = 0; j < N; j++) {
            A[nColloc][j] = evalBasis(j, DEGREE, a, knots);
        }
        rhs[nColloc] = ya;

        for (int j = 0; j < N; j++) {
            A[N - 1][j] = evalBasis(j, DEGREE, b, knots);
        }
        rhs[N - 1] = yb;
        //решаем систему методом Гаусса
        this.coefficients = solveLinearSystem(A, rhs);
    }


    //Вычисляет приближённое значение решения y(x) в заданной точке x.
    // y(x) ≈ Σ c_j * B_j(x)
    // c_j = coefficients[i], а B_j(x) = evalBasis(i, DEGREE, x, knots)
    @Override
    public double apply(double x) {
        double sum = 0.0; // sum = y(x)
        for (int i = 0; i < coefficients.length; i++) {
            sum += coefficients[i] * evalBasis(i, DEGREE, x, knots);
        }
        return sum;
    }

    /**
     * Рекурсивное вычисление базисной B-сплайн функции N_{i,k}(x) (или B_j(x))
     * по формуле Кокса–де Бура.
     * i - индекс базисной функции
     * k - тепень сплайна (0 = константа, 1 = линейная, ..., 3 = кубическая)
     * x - точка вычисления
     * knots - узловой вектор
     */
    static double evalBasis(int i, int k, double x, double[] knots) {
        // Базовый случай: сплайн нулевой степени — индикатор отрезка
        if (k == 0) {
            if (knots[i] <= x && x < knots[i + 1]) {
                return 1.0;
            }
            return 0.0;
        }
        // Рекурсивный случай: формула Кокса–де Бура
        double left = 0.0;//левое слагаемое
        double denomL = knots[i + k] - knots[i];
        if (Math.abs(denomL) > 1e-15) {
            left = ((x - knots[i]) / denomL) * evalBasis(i, k - 1, x, knots);
        }
        double right = 0.0;//правое слагаемое
        double denomR = knots[i + k + 1] - knots[i + 1];
        if (Math.abs(denomR) > 1e-15) {
            right = ((knots[i + k + 1] - x) / denomR) * evalBasis(i + 1, k - 1, x, knots);
        }
        return left + right;
    }

    /**
     * Аналитическое вычисление производной базисной B-сплайн функции.
     * Использует рекуррентную формулу:
     *   d/dx N_{i,k}(x) = k/(t_{i+k} - t_i) * N_{i,k-1}(x) - k/(t_{i+k+1} - t_{i+1}) * N_{i+1,k-1}(x)
     * i - индекс базисной функции
     * k - степень сплайна (всегда 3 в этом классе)
     * x - точка вычисления
     * knots - узловой вектор
     * order - порядок производной (1 или 2)
     * Численное вычисление производной базисной функции методом центральных разностей. Для повышения точности
     */
    static double derivBasis(int i, int k, double x, double[] knots, int order) {
        if (order == 1) {
            double h = 1e-6;
            return (evalBasis(i, k, x + h, knots) - evalBasis(i, k, x - h, knots)) / (2 * h);
        }
        if (order == 2) {
            double h = 1e-5;
            return (evalBasis(i, k, x + h, knots) - 2 * evalBasis(i, k, x, knots) + evalBasis(i, k, x - h, knots)) / (h * h);
        }
        return evalBasis(i, k, x, knots);
    }

    /**
     * Решает систему линейных уравнений A * x = b методом Гаусса
     * с частичным выбором ведущего элемента для численной устойчивости.
     * A - квадратная матрица коэффициентов
     * b - вектор правой части
     */
    private static double[] solveLinearSystem(double[][] A, double[] b) {
        int n = b.length;
        // Создаём расширенную матрицу [A | b]
        double[][] M = new double[n][n + 1];
        for (int i = 0; i < n; i++) {
            System.arraycopy(A[i], 0, M[i], 0, n);
            M[i][n] = b[i];
        }
        // Прямой ход
        for (int i = 0; i < n; i++) {
            // Частичный выбор ведущего (максимальный по модулю в столбце)
            int pivotRow = i;
            for (int k = i + 1; k < n; k++) {
                if (Math.abs(M[k][i]) > Math.abs(M[pivotRow][i])) {
                    pivotRow = k;
                }
            }

            // Меняем строки местами
            double[] tmp = M[i];
            M[i] = M[pivotRow];
            M[pivotRow] = tmp;

            //Проверка на вырожденность
            if (Math.abs(M[i][i]) < 1e-14) {
                //throw new RuntimeException("Вырожденная матрица на шаге " + i);
                M[i][i] = 1e-12;
            }

            // Обнуление под ведущим элементом
            for (int k = i + 1; k < n; k++) {
                double factor;
                factor = M[k][i] / M[i][i];
                for (int j = i; j <= n; j++) {
                    M[k][j] -= factor * M[i][j];
                }
            }
        }

        // Обратный ход
        double[] x = new double[n];
        for (int i = n - 1; i >= 0; i--) {
            x[i] = M[i][n];
            for (int j = i + 1; j < n; j++) {
                x[i] -= M[i][j] * x[j];
            }
            x[i] /= M[i][i];
        }
        return x;
    }
}