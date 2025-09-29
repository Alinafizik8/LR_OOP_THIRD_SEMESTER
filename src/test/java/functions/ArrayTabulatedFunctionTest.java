package functions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

public class ArrayTabulatedFunctionTest {

    // Вспомогательная функция для тестов: f(x) = x^2
    private static final MathFunction SQUARE = x -> x * x;

    // Тесты для первого конструктора: ArrayTabulatedFunction(double[], double[])

    @Test
    @DisplayName("Конструктор с массивами: корректная инициализация")
    public void testConstructorWithArrays() {
        double[] x = {1.0, 2.0, 3.0};
        double[] y = {1.0, 4.0, 9.0};
        ArrayTabulatedFunction func = new ArrayTabulatedFunction(x, y);

        assertEquals(3, func.getCount());
        assertEquals(1.0, func.getX(0));
        assertEquals(4.0, func.getY(1));
        assertEquals(1.0, func.leftBound());
        assertEquals(3.0, func.rightBound());
    }

    @Test
    @DisplayName("Конструктор с массивами: защита от изменения внешних массивов")
    public void testConstructorDefensiveCopy() {
        double[] x = {0.0, 1.0};
        double[] y = {0.0, 1.0};
        ArrayTabulatedFunction func = new ArrayTabulatedFunction(x, y);

        // Меняем исходные массивы
        x[0] = 999.0;
        y[0] = 888.0;

        // Убеждаемся, что функция не изменилась
        assertEquals(0.0, func.getX(0));
        assertEquals(0.0, func.getY(0));
    }

    @Test
    @DisplayName("Конструктор с массивами: выбрасывает исключение при null")
    public void testConstructorNullArrays() {
        assertThrows(IllegalArgumentException.class, () ->
                new ArrayTabulatedFunction(null, new double[]{1}));
        assertThrows(IllegalArgumentException.class, () ->
                new ArrayTabulatedFunction(new double[]{1}, null));
    }

    @Test
    @DisplayName("Конструктор с массивами: выбрасывает исключение при разной длине")
    public void testConstructorDifferentLengths() {
        assertThrows(IllegalArgumentException.class, () ->
                new ArrayTabulatedFunction(new double[]{1, 2}, new double[]{1}));
    }

    @Test
    @DisplayName("Конструктор с массивами: выбрасывает исключение при нестрогом возрастании x")
    public void testConstructorNonIncreasingX() {
        assertThrows(IllegalArgumentException.class, () ->
                new ArrayTabulatedFunction(new double[]{1, 1, 2}, new double[]{1, 1, 4}));
        assertThrows(IllegalArgumentException.class, () ->
                new ArrayTabulatedFunction(new double[]{2, 1}, new double[]{4, 1}));
    }

    // Тесты для второго конструктора: ArrayTabulatedFunction(MathFunction, double, double, int)

    @Test
    @DisplayName("Конструктор с функцией: равномерная дискретизация")
    public void testConstructorWithFunction() {
        ArrayTabulatedFunction func = new ArrayTabulatedFunction(SQUARE, 0.0, 2.0, 3);
        // Ожидаем точки: x = [0, 1, 2], y = [0, 1, 4]
        assertEquals(3, func.getCount());
        assertEquals(0.0, func.getX(0));
        assertEquals(1.0, func.getX(1));
        assertEquals(2.0, func.getX(2));
        assertEquals(0.0, func.getY(0));
        assertEquals(1.0, func.getY(1));
        assertEquals(4.0, func.getY(2));
    }

    @Test
    @DisplayName("Конструктор с функцией: автоматическая перестановка xFrom и xTo")
    public void testConstructorSwapsBounds() {
        ArrayTabulatedFunction func = new ArrayTabulatedFunction(SQUARE, 2.0, 0.0, 3);
        // Должно быть то же, что и при (0.0, 2.0, 3)
        assertEquals(0.0, func.getX(0));
        assertEquals(2.0, func.getX(2));
    }

    @Test
    @DisplayName("Конструктор с функцией: случай xFrom == xTo")
    public void testConstructorSinglePoint() {
        ArrayTabulatedFunction func = new ArrayTabulatedFunction(SQUARE, 5.0, 5.0, 4);
        for (int i = 0; i < 4; i++) {
            assertEquals(5.0, func.getX(i));
            assertEquals(25.0, func.getY(i));
        }
    }

    @Test
    @DisplayName("Конструктор с функцией: выбрасывает исключение при count <= 0")
    public void testConstructorInvalidCount() {
        assertThrows(IllegalArgumentException.class, () ->
                new ArrayTabulatedFunction(SQUARE, 0, 1, 0));
        assertThrows(IllegalArgumentException.class, () ->
                new ArrayTabulatedFunction(SQUARE, 0, 1, -1));
    }

    // Тесты методов доступа и модификации

    @Test
    @DisplayName("Метод setY корректно изменяет значение")
    public void testSetY() {
        ArrayTabulatedFunction func = new ArrayTabulatedFunction(new double[]{0, 1}, new double[]{0, 1});
        func.setY(1, 100.0);
        assertEquals(100.0, func.getY(1));
    }

    @Test
    @DisplayName("Методы indexOfX и indexOfY возвращают -1, если элемент не найден")
    public void testIndexOfNotFound() {
        ArrayTabulatedFunction func = new ArrayTabulatedFunction(new double[]{1, 2}, new double[]{1, 4});
        assertEquals(-1, func.indexOfX(1.5));
        assertEquals(-1, func.indexOfY(2.0));
    }

    @Test
    @DisplayName("Методы indexOfX и indexOfY находят точные совпадения")
    public void testIndexOfFound() {
        ArrayTabulatedFunction func = new ArrayTabulatedFunction(new double[]{1, 2}, new double[]{1, 4});
        assertEquals(0, func.indexOfX(1.0));
        assertEquals(1, func.indexOfY(4.0));
    }

    // Тесты floorIndexOfX

    @Test
    @DisplayName("floorIndexOfX: возвращает 0, если x меньше всех")
    public void testFloorIndexBelowAll() {
        ArrayTabulatedFunction func = new ArrayTabulatedFunction(new double[]{1, 2, 3}, new double[]{1, 4, 9});
        assertEquals(0, func.floorIndexOfX(0.5));
    }

    @Test
    @DisplayName("floorIndexOfX: возвращает count, если x больше всех")
    public void testFloorIndexAboveAll() {
        ArrayTabulatedFunction func = new ArrayTabulatedFunction(new double[]{1, 2, 3}, new double[]{1, 4, 9});
        assertEquals(3, func.floorIndexOfX(4.0));
    }

    @Test
    @DisplayName("floorIndexOfX: находит правильный индекс внутри")
    public void testFloorIndexInside() {
        ArrayTabulatedFunction func = new ArrayTabulatedFunction(new double[]{1, 2, 3}, new double[]{1, 4, 9});
        assertEquals(1, func.floorIndexOfX(2.5)); // между 2 и 3 → индекс 1
    }

    @Test
    @DisplayName("floorIndexOfX: работает при count = 1")
    public void testFloorIndexSinglePoint() {
        ArrayTabulatedFunction func = new ArrayTabulatedFunction(new double[]{5}, new double[]{25});
        assertEquals(0, func.floorIndexOfX(4.0)); // x < 5 → 0
        assertEquals(1, func.floorIndexOfX(6.0)); // x > 5 → count = 1
    }

    // Тесты apply(), интерполяции и экстраполяции

    @Test
    @DisplayName("apply() возвращает точное значение, если x есть в таблице")
    public void testApplyExactMatch() {
        ArrayTabulatedFunction func = new ArrayTabulatedFunction(new double[]{1, 2}, new double[]{1, 4});
        assertEquals(1.0, func.apply(1.0));
    }

    @Test
    @DisplayName("apply() выполняет интерполяцию внутри")
    public void testApplyInterpolation() {
        ArrayTabulatedFunction func = new ArrayTabulatedFunction(new double[]{0, 2}, new double[]{0, 4});
        // Линейная интерполяция: y = 2x → при x=1, y=2
        assertEquals(2.0, func.apply(1.0), 1e-12);
    }

    @Test
    @DisplayName("apply() выполняет экстраполяцию слева")
    public void testApplyExtrapolateLeft() {
        ArrayTabulatedFunction func = new ArrayTabulatedFunction(new double[]{1, 2}, new double[]{1, 4});
        // Экстраполяция через (1,1) и (2,4): наклон = 3 → y = 3x - 2
        // При x=0: y = -2
        assertEquals(-2.0, func.apply(0.0), 1e-12);
    }

    @Test
    @DisplayName("apply() выполняет экстраполяцию справа")
    public void testApplyExtrapolateRight() {
        ArrayTabulatedFunction func = new ArrayTabulatedFunction(new double[]{1, 2}, new double[]{1, 4});
        // При x=3: y = 3*3 - 2 = 7
        assertEquals(7.0, func.apply(3.0), 1e-12);
    }

    @Test
    @DisplayName("apply() возвращает единственное значение при count = 1")
    public void testApplySinglePoint() {
        ArrayTabulatedFunction func = new ArrayTabulatedFunction(new double[]{5}, new double[]{25});
        assertEquals(25.0, func.apply(0.0));   // экстраполяция слева
        assertEquals(25.0, func.apply(5.0));   // точное совпадение
        assertEquals(25.0, func.apply(10.0));  // экстраполяция справа
    }
}