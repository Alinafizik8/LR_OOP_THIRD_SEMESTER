package functions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

public class ArrayTabulatedFunctionTest {

    private static final MathFunction SQUARE = x -> x * x;

    // ============================================================
    // Тесты первого конструктора: ArrayTabulatedFunction(double[], double[])
    // ============================================================

    @Test
    @DisplayName("Конструктор с массивами: успешная инициализация")
    void testConstructorWithValidArrays() {
        double[] x = {1.0, 2.0, 3.0};
        double[] y = {1.0, 4.0, 9.0};
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(x, y);
        assertEquals(3, f.getCount());
        assertEquals(1.0, f.getX(0));
        assertEquals(4.0, f.getY(1));
    }

    @Test
    @DisplayName("Конструктор с массивами: защита от изменения внешних массивов")
    void testConstructorDefensiveCopy() {
        double[] x = {0.0, 1.0};
        double[] y = {0.0, 1.0};
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(x, y);
        x[0] = 999.0;
        y[0] = 888.0;
        assertEquals(0.0, f.getX(0));
        assertEquals(0.0, f.getY(0));
    }

    @Test
    @DisplayName("Конструктор с массивами: выбрасывает исключение при null")
    void testConstructorNullArrays() {
        assertThrows(IllegalArgumentException.class, () ->
                new ArrayTabulatedFunction(null, new double[]{1}));
        assertThrows(IllegalArgumentException.class, () ->
                new ArrayTabulatedFunction(new double[]{1}, null));
    }

    @Test
    @DisplayName("Конструктор с массивами: выбрасывает исключение при разной длине")
    void testConstructorDifferentLengths() {
        assertThrows(IllegalArgumentException.class, () ->
                new ArrayTabulatedFunction(new double[]{1, 2}, new double[]{1}));
    }

    @Test
    @DisplayName("Конструктор с массивами: выбрасывает исключение при пустых массивах")
    void testConstructorEmptyArrays() {
        assertThrows(IllegalArgumentException.class, () ->
                new ArrayTabulatedFunction(new double[]{}, new double[]{}));
    }

    @Test
    @DisplayName("Конструктор с массивами: выбрасывает исключение при нестрогом возрастании x")
    void testConstructorNonStrictlyIncreasingX() {
        assertThrows(IllegalArgumentException.class, () ->
                new ArrayTabulatedFunction(new double[]{1, 1, 2}, new double[]{1, 1, 4}));
        assertThrows(IllegalArgumentException.class, () ->
                new ArrayTabulatedFunction(new double[]{2, 1}, new double[]{4, 1}));
    }

    // ============================================================
    // Тесты второго конструктора: ArrayTabulatedFunction(MathFunction, double, double, int)
    // ============================================================

    @Test
    @DisplayName("Конструктор с функцией: равномерная дискретизация")
    void testConstructorWithFunction() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(SQUARE, 0.0, 2.0, 3);
        assertEquals(0.0, f.getX(0));
        assertEquals(1.0, f.getX(1));
        assertEquals(2.0, f.getX(2));
        assertEquals(0.0, f.getY(0));
        assertEquals(1.0, f.getY(1));
        assertEquals(4.0, f.getY(2));
    }

    @Test
    @DisplayName("Конструктор с функцией: автоматическая перестановка xFrom и xTo")
    void testConstructorSwapsBounds() {
        ArrayTabulatedFunction f1 = new ArrayTabulatedFunction(SQUARE, 0.0, 2.0, 3);
        ArrayTabulatedFunction f2 = new ArrayTabulatedFunction(SQUARE, 2.0, 0.0, 3);
        assertEquals(f1.getX(0), f2.getX(0));
        assertEquals(f1.getX(2), f2.getX(2));
    }

    @Test
    @DisplayName("Конструктор с функцией: случай xFrom == xTo")
    void testConstructorSinglePoint() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(SQUARE, 5.0, 5.0, 4);
        for (int i = 0; i < 4; i++) {
            assertEquals(5.0, f.getX(i));
            assertEquals(25.0, f.getY(i));
        }
    }

    @Test
    @DisplayName("Конструктор с функцией: выбрасывает исключение при count <= 0")
    void testConstructorInvalidCount() {
        assertThrows(IllegalArgumentException.class, () ->
                new ArrayTabulatedFunction(SQUARE, 0, 1, 0));
        assertThrows(IllegalArgumentException.class, () ->
                new ArrayTabulatedFunction(SQUARE, 0, 1, -1));
    }

    @Test
    @DisplayName("Конструктор с функцией: выбрасывает исключение при null source")
    void testConstructorNullSource() {
        assertThrows(IllegalArgumentException.class, () ->
                new ArrayTabulatedFunction(null, 0, 1, 1));
    }

    // ============================================================
    // Тесты методов доступа
    // ============================================================

    @Test
    @DisplayName("getCount возвращает корректное значение")
    void testGetCount() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(new double[]{1}, new double[]{1});
        assertEquals(1, f.getCount());
    }

    @Test
    @DisplayName("getX выбрасывает исключение при недопустимом индексе")
    void testGetXInvalidIndex() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(new double[]{1}, new double[]{1});
        assertThrows(IndexOutOfBoundsException.class, () -> f.getX(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> f.getX(1));
    }

    @Test
    @DisplayName("getY выбрасывает исключение при недопустимом индексе")
    void testGetYInvalidIndex() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(new double[]{1}, new double[]{1});
        assertThrows(IndexOutOfBoundsException.class, () -> f.getY(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> f.getY(1));
    }

    @Test
    @DisplayName("setY корректно устанавливает значение")
    void testSetY() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(new double[]{0, 1}, new double[]{0, 1});
        f.setY(1, 100.0);
        assertEquals(100.0, f.getY(1));
    }

    @Test
    @DisplayName("setY выбрасывает исключение при недопустимом индексе")
    void testSetYInvalidIndex() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(new double[]{1}, new double[]{1});
        assertThrows(IndexOutOfBoundsException.class, () -> f.setY(-1, 0));
        assertThrows(IndexOutOfBoundsException.class, () -> f.setY(1, 0));
    }

    @Test
    @DisplayName("leftBound и rightBound возвращают корректные границы")
    void testBounds() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(new double[]{1, 3}, new double[]{1, 9});
        assertEquals(1.0, f.leftBound());
        assertEquals(3.0, f.rightBound());
    }

    // ============================================================
    // Тесты поиска
    // ============================================================

    @Test
    @DisplayName("indexOfX находит точное совпадение")
    void testIndexOfXFound() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(new double[]{1, 2}, new double[]{1, 4});
        assertEquals(0, f.indexOfX(1.0));
    }

    @Test
    @DisplayName("indexOfX возвращает -1, если x не найден")
    void testIndexOfXNotFound() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(new double[]{1, 2}, new double[]{1, 4});
        assertEquals(-1, f.indexOfX(1.5));
    }

    @Test
    @DisplayName("indexOfY находит точное совпадение")
    void testIndexOfYFound() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(new double[]{1, 2}, new double[]{1, 4});
        assertEquals(1, f.indexOfY(4.0));
    }

    @Test
    @DisplayName("indexOfY возвращает -1, если y не найден")
    void testIndexOfYNotFound() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(new double[]{1, 2}, new double[]{1, 4});
        assertEquals(-1, f.indexOfY(2.0));
    }

    // ============================================================
    // Тесты floorIndexOfX
    // ============================================================

    @Test
    @DisplayName("floorIndexOfX: возвращает 0, если x <= первого элемента")
    void testFloorIndexBelowOrEqualFirst() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(new double[]{1, 2, 3}, new double[]{1, 4, 9});
        assertEquals(0, f.floorIndexOfX(0.5));
        assertEquals(0, f.floorIndexOfX(1.0));
    }

    @Test
    @DisplayName("floorIndexOfX: возвращает count, если x > последнего элемента")
    void testFloorIndexAboveLast() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(new double[]{1, 2, 3}, new double[]{1, 4, 9});
        assertEquals(3, f.floorIndexOfX(4.0));
    }

    @Test
    @DisplayName("floorIndexOfX: находит правильный индекс внутри")
    void testFloorIndexInside() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(new double[]{1, 2, 3}, new double[]{1, 4, 9});
        assertEquals(1, f.floorIndexOfX(2.5));
    }

    @Test
    @DisplayName("floorIndexOfX: работает при count = 1")
    void testFloorIndexSinglePoint() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(new double[]{5}, new double[]{25});
        assertEquals(0, f.floorIndexOfX(4.0));
        assertEquals(1, f.floorIndexOfX(6.0));
    }

    // ============================================================
    // Тесты интерполяции и экстраполяции
    // ============================================================

    @Test
    @DisplayName("interpolate с floorIndex: корректная линейная интерполяция")
    void testInterpolateWithIndex() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(new double[]{0, 2}, new double[]{0, 4});
        assertEquals(2.0, f.interpolate(1.0, 0), 1e-12);
    }

    @Test
    @DisplayName("interpolate с floorIndex: выбрасывает исключение при недопустимом индексе")
    void testInterpolateInvalidIndex() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(new double[]{0, 1}, new double[]{0, 1});
        assertThrows(IndexOutOfBoundsException.class, () -> f.interpolate(0.5, -1));
        assertThrows(IndexOutOfBoundsException.class, () -> f.interpolate(0.5, 1));
    }

    @Test
    @DisplayName("extrapolateLeft: линейная экстраполяция слева")
    void testExtrapolateLeft() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(new double[]{1, 2}, new double[]{1, 4});
        // Наклон = 3, y = 3x - 2 → при x=0: y=-2
        assertEquals(-2.0, f.extrapolateLeft(0.0), 1e-12);
    }

    @Test
    @DisplayName("extrapolateRight: линейная экстраполяция справа")
    void testExtrapolateRight() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(new double[]{1, 2}, new double[]{1, 4});
        // При x=3: y = 3*3 - 2 = 7
        assertEquals(7.0, f.extrapolateRight(3.0), 1e-12);
    }

    @Test
    @DisplayName("Экстраполяция и интерполяция возвращают единственное значение при count = 1")
    void testSinglePointInterpolationExtrapolation() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(new double[]{5}, new double[]{25});
        assertEquals(25.0, f.interpolate(10.0, 0));
        assertEquals(25.0, f.extrapolateLeft(0.0));
        assertEquals(25.0, f.extrapolateRight(10.0));
    }

    // ============================================================
    // Тест apply() из AbstractTabulatedFunction (неявно)
    // ============================================================

    @Test
    @DisplayName("apply() использует точное значение, если x есть в таблице")
    void testApplyExactMatch() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(new double[]{1, 2}, new double[]{1, 4});
        assertEquals(1.0, f.apply(1.0));
    }

    @Test
    @DisplayName("apply() использует интерполяцию внутри")
    void testApplyInterpolation() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(new double[]{0, 2}, new double[]{0, 4});
        assertEquals(2.0, f.apply(1.0), 1e-12);
    }

    @Test
    @DisplayName("apply() использует экстраполяцию слева")
    void testApplyExtrapolateLeft() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(new double[]{1, 2}, new double[]{1, 4});
        assertEquals(-2.0, f.apply(0.0), 1e-12);
    }

    @Test
    @DisplayName("apply() использует экстраполяцию справа")
    void testApplyExtrapolateRight() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(new double[]{1, 2}, new double[]{1, 4});
        assertEquals(7.0, f.apply(3.0), 1e-12);
    }
}