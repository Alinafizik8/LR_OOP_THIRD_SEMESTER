package functions;
import java.util.function.Function;

// Импорт функционального интерфейса для передачи p(x), q(x), f(x)
import java.util.function.Function;

/**
 * Класс BSpline реализует решение линейного ОДУ второго порядка
 * методом кубических B-сплайнов (метод коллокации).
 * Реализует интерфейс MathFunction, чтобы можно было вызывать apply(x)
 * и получать значение приближённого решения в точке x.
 */
public class BSpline implements MathFunction {

    // Узловой вектор сплайна: определяет, где "склеиваются" полиномиальные участки
    private final double[] knots;

    // Коэффициенты линейной комбинации базисных B-сплайнов: y(x) = Σ c_j * B_j(x)
    private final double[] coefficients;

    // Степень сплайна: 3 = кубический (обеспечивает C²-гладкость)
    private static final int DEGREE = 3;

    /**
     * Конструктор: сразу решает ОДУ и сохраняет коэффициенты сплайна.
     *
     * @param a левая граница отрезка
     * @param b правая граница отрезка
     * @param nIntervals количество подынтервалов разбиения [a, b]
     * @param p функция p(x) из уравнения y'' + p(x)y' + q(x)y = f(x)
     * @param q функция q(x)
     * @param f правая часть уравнения f(x)
     * @param ya значение решения на левой границе: y(a) = ya
     * @param yb значение решения на правой границе: y(b) = yb
     */
    public BSpline(double a, double b, int nIntervals,
                   Function<Double, Double> p,
                   Function<Double, Double> q,
                   Function<Double, Double> f,
                   double ya, double yb) {

        // === ШАГ 1: Построение узлового вектора ===

        // Создаём равномерную внутреннюю сетку из (nIntervals + 1) точек
        double[] interior = new double[nIntervals + 1];
        for (int i = 0; i <= nIntervals; i++) {
            // Точка i-го узла: линейная интерполяция между a и b
            interior[i] = a + (b - a) * i / nIntervals;
        }

        // Общее число узлов: 2*(DEGREE) "фиктивных" + внутренние
        // Для кубического сплайна (DEGREE=3) нужно 4 узла в начале и 4 в конце
        int totalKnots = 2 * DEGREE + interior.length;
        knots = new double[totalKnots];

        // Заполняем первые DEGREE узлов значением a (кратность = DEGREE)
        for (int i = 0; i < DEGREE; i++) {
            knots[i] = a;
        }

        // Копируем внутренние узлы в середину вектора
        System.arraycopy(interior, 0, knots, DEGREE, interior.length);

        // Заполняем последние DEGREE узлов значением b
        for (int i = DEGREE + interior.length; i < totalKnots; i++) {
            knots[i] = b;
        }

        // Число базисных B-сплайнов: N = (число узлов) - (степень) - 1
        int N = knots.length - DEGREE - 1;

        // === ШАГ 2: Выбор коллокационных точек ===

        // Коллокационных уравнений будет N - 2 (остальные 2 — граничные условия)
        int nColloc = Math.max(1, N - 2);
        double[] colloc = new double[nColloc];

        if (nColloc == 1) {
            // Если сетка очень грубая — берём середину отрезка
            colloc[0] = (a + b) / 2.0;
        } else {
            // Равномерно распределяем коллокационные точки внутри (a, b)
            for (int i = 0; i < nColloc; i++) {
                colloc[i] = a + (b - a) * (i + 1.0) / (nColloc + 1);
            }
        }

        // === ШАГ 3: Формирование системы линейных уравнений A * c = rhs ===

        // Матрица системы: N уравнений, N неизвестных (коэффициентов)
        double[][] A = new double[N][N];
        // Правая часть системы
        double[] rhs = new double[N];

        // Заполняем строки, соответствующие коллокационным уравнениям
        for (int idx = 0; idx < nColloc; idx++) {
            // Первая и последняя строки зарезервированы под граничные условия
            int row = idx + 1;
            // Текущая коллокационная точка
            double x = colloc[idx];
            // Вычисляем значения коэффициентов уравнения в точке x
            double px = p.apply(x);
            double qx = q.apply(x);
            // Правая часть уравнения в точке x
            rhs[row] = f.apply(x);

            // Для каждого базисного сплайна B_j(x) вычисляем его вклад в уравнение
            for (int j = 0; j < N; j++) {
                // Значение базисной функции B_j(x)
                double b0 = evalBasis(j, DEGREE, x, knots);
                // Первая производная B_j'(x) (численно)
                double b1 = derivBasis(j, DEGREE, x, knots, 1);
                // Вторая производная B_j''(x) (численно)
                double b2 = derivBasis(j, DEGREE, x, knots, 2);
                // Подставляем в ОДУ: B_j'' + p*B_j' + q*B_j
                A[row][j] = b2 + px * b1 + qx * b0;
            }
        }

        // === ШАГ 4: Добавление граничных условий ===

        // Первая строка: y(a) = ya
        for (int j = 0; j < N; j++) {
            // B_j(a) — значение j-го базисного сплайна в точке a
            A[0][j] = evalBasis(j, DEGREE, a, knots);
        }
        rhs[0] = ya; // Заданное значение на левой границе

        // Последняя строка: y(b) = yb
        for (int j = 0; j < N; j++) {
            A[N - 1][j] = evalBasis(j, DEGREE, b, knots);
        }
        rhs[N - 1] = yb; // Заданное значение на правой границе

        // === ШАГ 5: Решение системы линейных уравнений ===

        // Решаем A * c = rhs методом Гаусса и сохраняем коэффициенты
        this.coefficients = solveLinearSystem(A, rhs);
    }

    /**
     * Реализация интерфейса MathFunction.
     * Возвращает значение приближённого решения y(x) в заданной точке x.
     */
    @Override
    public double apply(double x) {
        double sum = 0.0;
        // Вычисляем линейную комбинацию: y(x) = Σ c_j * B_j(x)
        for (int i = 0; i < coefficients.length; i++) {
            sum += coefficients[i] * evalBasis(i, DEGREE, x, knots);
        }
        return sum;
    }

    /**
     * Рекурсивное вычисление базисной B-сплайн функции N_{i,k}(x)
     * по формуле Кокса–де Бура.
     *
     * @param i индекс базисной функции
     * @param k степень сплайна (0 = константа, 1 = линейная, и т.д.)
     * @param x точка, в которой вычисляем
     * @param knots узловой вектор
     * @return значение базисной функции
     */
    private static double evalBasis(int i, int k, double x, double[] knots) {
        // Базовый случай: сплайн нулевой степени — индикатор отрезка [t_i, t_{i+1})
        if (k == 0) {
            // Обычное условие: x в полуинтервале [t_i, t_{i+1})
            if (knots[i] <= x && x < knots[i + 1]) {
                return 1.0;
            }
            // Особый случай: если x точно совпадает с последним узлом — разрешаем
            if (i == knots.length - k - 2 && Math.abs(x - knots[i + 1]) < 1e-12) {
                return 1.0;
            }
            return 0.0;
        }

        // Рекурсивный случай: вычисляем левую часть формулы
        double left = 0.0;
        double denomL = knots[i + k] - knots[i]; // знаменатель для левой дроби
        if (denomL > 1e-12) { // избегаем деления на ноль
            left = (x - knots[i]) / denomL * evalBasis(i, k - 1, x, knots);
        }

        // Рекурсивный случай: вычисляем правую часть формулы
        double right = 0.0;
        double denomR = knots[i + k + 1] - knots[i + 1]; // знаменатель для правой дроби
        if (denomR > 1e-12) {
            right = (knots[i + k + 1] - x) / denomR * evalBasis(i + 1, k - 1, x, knots);
        }

        // Сумма левой и правой частей — значение сплайна степени k
        return left + right;
    }

    /**
     * Численное вычисление производной базисной функции заданного порядка.
     * Используется метод конечных разностей.
     *
     * @param i индекс базисной функции
     * @param k степень сплайна (всегда DEGREE = 3)
     * @param x точка вычисления
     * @param knots узловой вектор
     * @param order порядок производной (0, 1 или 2)
     * @return значение производной
     */
    private static double derivBasis(int i, int k, double x, double[] knots, int order) {
        double h = 1e-6; // шаг для численного дифференцирования

        if (order == 0) {
            // Нулевая производная — сама функция
            return evalBasis(i, k, x, knots);
        }
        if (order == 1) {
            // Центральная разность для первой производной
            return (evalBasis(i, k, x + h, knots) - evalBasis(i, k, x - h, knots)) / (2 * h);
        }
        if (order == 2) {
            // Центральная разность для второй производной
            return (evalBasis(i, k, x + h, knots) - 2 * evalBasis(i, k, x, knots) +
                    evalBasis(i, k, x - h, knots)) / (h * h);
        }
        // Если запрошен недопустимый порядок — ошибка
        throw new IllegalArgumentException("Поддерживаемые порядки: 0, 1, 2");
    }

    /**
     * Решение системы линейных уравнений A * x = b методом Гаусса
     * с частичным выбором ведущего элемента (для устойчивости).
     *
     * @param A квадратная матрица коэффициентов
     * @param b вектор правой части
     * @return решение x
     */
    private static double[] solveLinearSystem(double[][] A, double[] b) {
        int n = b.length;
        // Создаём расширенную матрицу [A | b]
        double[][] M = new double[n][n + 1];
        for (int i = 0; i < n; i++) {
            // Копируем строку матрицы A
            System.arraycopy(A[i], 0, M[i], 0, n);
            // Добавляем элемент правой части
            M[i][n] = b[i];
        }

        // === Прямой ход метода Гаусса ===
        for (int i = 0; i < n; i++) {
            // Находим строку с максимальным по модулю элементом в столбце i (выбор ведущего)
            int pivotRow = i;
            for (int k = i + 1; k < n; k++) {
                if (Math.abs(M[k][i]) > Math.abs(M[pivotRow][i])) {
                    pivotRow = k;
                }
            }
            // Меняем местами текущую строку и строку с ведущим элементом
            double[] tmp = M[i];
            M[i] = M[pivotRow];
            M[pivotRow] = tmp;

            // Обнуляем элементы под ведущим в столбце i
            for (int k = i + 1; k < n; k++) {
                double factor = M[k][i] / M[i][i]; // множитель для вычитания
                for (int j = i; j <= n; j++) {
                    M[k][j] -= factor * M[i][j]; // вычитаем строку
                }
            }
        }

        // === Обратный ход: находим решение снизу вверх ===
        double[] x = new double[n];
        for (int i = n - 1; i >= 0; i--) {
            x[i] = M[i][n]; // начинаем с правой части
            for (int j = i + 1; j < n; j++) {
                x[i] -= M[i][j] * x[j]; // вычитаем уже найденные компоненты
            }
            x[i] /= M[i][i]; // делим на диагональный элемент
        }
        return x;
    }
}
