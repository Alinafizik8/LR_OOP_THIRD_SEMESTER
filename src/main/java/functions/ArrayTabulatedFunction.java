package functions;

import java.util.Arrays;

/**
 * Реализация табулированной функции на основе массивов.
 */
public class ArrayTabulatedFunction extends AbstractTabulatedFunction {
    private double[] xValues;
    private double[] yValues;
    private int count;

    /**
     * Конструктор с двумя массивами.
     * Создаёт копии входных массивов для защиты от внешних изменений.
     *
     * @param xValues массив значений x (должен быть строго возрастающим)
     * @param yValues массив значений y
     * @throws IllegalArgumentException если массивы null, разной длины или пустые
     */
    public ArrayTabulatedFunction(double[] xValues, double[] yValues) {
        if (xValues == null || yValues == null) {
            throw new IllegalArgumentException("Arrays must not be null");
        }
        if (xValues.length != yValues.length) {
            throw new IllegalArgumentException("Arrays must have the same length");
        }
        if (xValues.length == 0) {
            throw new IllegalArgumentException("Arrays must not be empty");
        }
        // Проверка на строгое возрастание (опционально, но рекомендуется)
        for (int i = 1; i < xValues.length; i++) {
            if (xValues[i] <= xValues[i - 1]) {
                throw new IllegalArgumentException("xValues must be strictly increasing");
            }
        }

        this.xValues = Arrays.copyOf(xValues, xValues.length);
        this.yValues = Arrays.copyOf(yValues, yValues.length);
        this.count = xValues.length;
    }

    /**
     * Конструктор с дискретизацией функции.
     *
     * @param source функция для табуляции
     * @param xFrom  левая граница интервала
     * @param xTo    правая граница интервала
     * @param count  количество точек
     */
    public ArrayTabulatedFunction(MathFunction source, double xFrom, double xTo, int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("Count must be positive");
        }
        if (source == null) {
            throw new IllegalArgumentException("Source function must not be null");
        }

        // Меняем местами, если xFrom > xTo
        if (xFrom > xTo) {
            double temp = xFrom;
            xFrom = xTo;
            xTo = temp;
        }

        this.count = count;
        this.xValues = new double[count];
        this.yValues = new double[count];

        if (xFrom == xTo) {
            // Все точки совпадают
            double y = source.apply(xFrom);
            Arrays.fill(xValues, xFrom);
            Arrays.fill(yValues, y);
        } else {
            // Равномерная дискретизация
            double step = (xTo - xFrom) / (count - 1);
            for (int i = 0; i < count; i++) {
                xValues[i] = xFrom + i * step;
                yValues[i] = source.apply(xValues[i]);
            }
        }
    }

    @Override
    protected int getCount() {
        return count;
    }

    @Override
    protected double getX(int index) {
        if (index < 0 || index >= count) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + count);
        }
        return xValues[index];
    }

    @Override
    protected double getY(int index) {
        if (index < 0 || index >= count) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + count);
        }
        return yValues[index];
    }

    /**
     * Устанавливает новое значение y по индексу.
     */
    public void setY(int index, double y) {
        if (index < 0 || index >= count) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + count);
        }
        yValues[index] = y;
    }

    /**
     * Возвращает левую границу области определения.
     */
    public double leftBound() {
        return xValues[0];
    }

    /**
     * Возвращает правую границу области определения.
     */
    public double rightBound() {
        return xValues[count - 1];
    }

    @Override
    protected int indexOfX(double x) {
        for (int i = 0; i < count; i++) {
            if (xValues[i] == x) {
                return i;
            }
        }
        return -1;
    }

    protected int indexOfY(double y) {
        for (int i = 0; i < count; i++) {
            if (yValues[i] == y) {
                return i;
            }
        }
        return -1;
    }

    @Override
    protected int floorIndexOfX(double x) {
        if (count == 1) {
            return x < xValues[0] ? 0 : count; // но count=1, так что 1
        }

        if (x <= xValues[0]) {
            return 0;
        }
        if (x > xValues[count - 1]) {
            return count;
        }

        // Бинарный поиск для эффективности (можно и линейный, но бинарный лучше)
        int low = 0;
        int high = count - 1;

        while (low <= high) {
            int mid = (low + high) / 2;
            if (xValues[mid] < x) {
                if (mid == count - 1 || xValues[mid + 1] >= x) {
                    return mid;
                }
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        return 0; // fallback (не должно происходить)
    }

    @Override
    protected double extrapolateLeft(double x) {
        if (count == 1) {
            return yValues[0];
        }
        // Линейная экстраполяция слева через первые две точки
        return interpolate(x, xValues[0], xValues[1], yValues[0], yValues[1]);
    }

    @Override
    protected double extrapolateRight(double x) {
        if (count == 1) {
            return yValues[0];
        }
        // Линейная экстраполяция справа через последние две точки
        int lastIndex = count - 1;
        return interpolate(x, xValues[lastIndex - 1], xValues[lastIndex],
                yValues[lastIndex - 1], yValues[lastIndex]);
    }

    @Override
    protected double interpolate(double x, int floorIndex) {
        if (count == 1) {
            return yValues[0];
        }
        if (floorIndex < 0 || floorIndex >= count - 1) {
            throw new IndexOutOfBoundsException("Invalid floorIndex: " + floorIndex);
        }
        return interpolate(x, xValues[floorIndex], xValues[floorIndex + 1],
                yValues[floorIndex], yValues[floorIndex + 1]);
    }
}