package functions;

import java.util.Arrays;
import java.util.Iterator;

public class ArrayTabulatedFunction extends AbstractTabulatedFunction {
    private double[] xValues;
    private double[] yValues;
    private int count;

    /**
     * Конструктор с двумя массивами.
     * Создаёт копии входных массивов для защиты от внешних изменений.
     * xValues массив значений x (должен быть строго возрастающим)
     * yValues массив значений y
     * IllegalArgumentException если массивы null, разной длины или пустые
     */

    public void insert(double x, double y) {
        // 1. Проверяем, существует ли x
        for (int i = 0; i < count; i++) {
            if (Math.abs(xValues[i] - x) < 1e-10) {
                yValues[i] = y;
                return;
            }
        }

        // 2. Находим позицию для вставки
        int insertIndex = count; // по умолчанию — в конец
        for (int i = 0; i < count; i++) {
            if (x < xValues[i]) {
                insertIndex = i;
                break;
            }
        }

        // 3. Создаём новые массивы
        double[] newX = new double[count + 1];
        double[] newY = new double[count + 1];

        // 4. Копируем данные до insertIndex
        System.arraycopy(xValues, 0, newX, 0, insertIndex);
        System.arraycopy(yValues, 0, newY, 0, insertIndex);

        // 5. Вставляем новое значение
        newX[insertIndex] = x;
        newY[insertIndex] = y;

        // 6. Копируем остаток
        System.arraycopy(xValues, insertIndex, newX, insertIndex + 1, count - insertIndex);
        System.arraycopy(yValues, insertIndex, newY, insertIndex + 1, count - insertIndex);

        // 7. Обновляем поля
        this.xValues = newX;
        this.yValues = newY;
        this.count++;
    }

    //@Override
    public Iterator<Point> iterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void remove(int index) {
        if (index < 0 || index >= count) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + count);
        }
        if (count == 1) {
            // После удаления список станет пустым — но по условию задачи,
            // табулированная функция не может быть пустой.
            // Однако, если разрешено, можно обнулить.
            // Но лучше запретить удаление последнего элемента.
            throw new IllegalStateException("Cannot remove the last point from a tabulated function");
        }

        // Создаём новые массивы меньшего размера
        double[] newX = new double[count - 1];
        double[] newY = new double[count - 1];

        // Копируем элементы до index
        System.arraycopy(xValues, 0, newX, 0, index);
        System.arraycopy(yValues, 0, newY, 0, index);

        // Копируем элементы после index
        System.arraycopy(xValues, index + 1, newX, index, count - index - 1);
        System.arraycopy(yValues, index + 1, newY, index, count - index - 1);

        this.xValues = newX;
        this.yValues = newY;
        this.count = count - 1;
    }

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
        // Проверка на строгое возрастание
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
     * Конструктор с дискретизацией функции
     * source функция для табуляции
     * xFrom  левая граница интервала
     * xTo    правая граница интервала
     * count  количество точек
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
        return 0;
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