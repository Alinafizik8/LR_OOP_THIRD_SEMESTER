package functions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ArrayTabulatedFunctionTest {

    private static final MathFunction SQUARE = x -> x * x;

    // Тесты первого конструктора: ArrayTabulatedFunction(double[], double[])

    @Test
    void testConstructorWithValidArrays() {
        double[] x = {1.0, 2.0, 3.0};
        double[] y = {1.0, 4.0, 9.0};
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(x, y);
        assertEquals(3, f.getCount());
        assertEquals(1.0, f.getX(0));
        assertEquals(4.0, f.getY(1));
    }

    @Test
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
    void testConstructorNullArrays() {
        assertThrows(IllegalArgumentException.class, () ->
                new ArrayTabulatedFunction(null, new double[]{1}));
        assertThrows(IllegalArgumentException.class, () ->
                new ArrayTabulatedFunction(new double[]{1}, null));
    }

    @Test
    void testConstructorDifferentLengths() {
        assertThrows(IllegalArgumentException.class, () ->
                new ArrayTabulatedFunction(new double[]{1, 2}, new double[]{1}));
    }

    @Test

    void testConstructorEmptyArrays() {
        assertThrows(IllegalArgumentException.class, () ->
                new ArrayTabulatedFunction(new double[]{}, new double[]{}));
    }

    @Test
    void testConstructorNonStrictlyIncreasingX() {
        assertThrows(IllegalArgumentException.class, () ->
                new ArrayTabulatedFunction(new double[]{1, 1, 2}, new double[]{1, 1, 4}));
        assertThrows(IllegalArgumentException.class, () ->
                new ArrayTabulatedFunction(new double[]{2, 1}, new double[]{4, 1}));
    }

    // Тесты второго конструктора: ArrayTabulatedFunction(MathFunction, double, double, int)

    @Test
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
    void testConstructorSwapsBounds() {
        ArrayTabulatedFunction f1 = new ArrayTabulatedFunction(SQUARE, 0.0, 2.0, 3);
        ArrayTabulatedFunction f2 = new ArrayTabulatedFunction(SQUARE, 2.0, 0.0, 3);
        assertEquals(f1.getX(0), f2.getX(0));
        assertEquals(f1.getX(2), f2.getX(2));
    }

    @Test
    void testConstructorSinglePoint() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(SQUARE, 5.0, 5.0, 4);
        for (int i = 0; i < 4; i++) {
            assertEquals(5.0, f.getX(i));
            assertEquals(25.0, f.getY(i));
        }
    }

    @Test
    void testConstructorInvalidCount() {
        assertThrows(IllegalArgumentException.class, () ->
                new ArrayTabulatedFunction(SQUARE, 0, 1, 0));
        assertThrows(IllegalArgumentException.class, () ->
                new ArrayTabulatedFunction(SQUARE, 0, 1, -1));
    }

    @Test
    void testConstructorNullSource() {
        assertThrows(IllegalArgumentException.class, () ->
                new ArrayTabulatedFunction(null, 0, 1, 1));
    }

    // Тесты методов доступа

    @Test
    void testGetCount() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(new double[]{1,2}, new double[]{1,2});
        assertEquals(2, f.getCount());
    }

    @Test
    void testGetXInvalidIndex() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(new double[]{1,3}, new double[]{1,5});
        assertThrows(IndexOutOfBoundsException.class, () -> f.getX(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> f.getX(2));
    }

    @Test
    void testGetYInvalidIndex() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(new double[]{1,2}, new double[]{1,4});
        assertThrows(IndexOutOfBoundsException.class, () -> f.getY(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> f.getY(2));
    }

    @Test
    void testSetY() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(new double[]{0, 1}, new double[]{0, 1});
        f.setY(1, 100.0);
        assertEquals(100.0, f.getY(1));
    }

    @Test
    void testSetYInvalidIndex() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(new double[]{1,2}, new double[]{1,3});
        assertThrows(IndexOutOfBoundsException.class, () -> f.setY(-1, 0));
        assertThrows(IndexOutOfBoundsException.class, () -> f.setY(2, 0));
    }

    @Test
    void testBounds() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(new double[]{1, 3}, new double[]{1, 9});
        assertEquals(1.0, f.leftBound());
        assertEquals(3.0, f.rightBound());
    }

    // Тесты поиска

    @Test
    void testIndexOfXFound() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(new double[]{1, 2}, new double[]{1, 4});
        assertEquals(0, f.indexOfX(1.0));
    }

    @Test
    void testIndexOfXNotFound() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(new double[]{1, 2}, new double[]{1, 4});
        assertEquals(-1, f.indexOfX(1.5));
    }

    @Test
    void testIndexOfYFound() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(new double[]{1, 2}, new double[]{1, 4});
        assertEquals(1, f.indexOfY(4.0));
    }

    @Test
    void testIndexOfYNotFound() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(new double[]{1, 2}, new double[]{1, 4});
        assertEquals(-1, f.indexOfY(2.0));
    }

    // Тесты floorIndexOfX

    @Test
    void testFloorIndexBelowOrEqualFirst() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(new double[]{1, 2, 3}, new double[]{1, 4, 9});
        assertEquals(0, f.floorIndexOfX(0.5));
        assertEquals(0, f.floorIndexOfX(1.0));
    }

    @Test
    void testFloorIndexAboveLast() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(new double[]{1, 2, 3}, new double[]{1, 4, 9});
        assertEquals(3, f.floorIndexOfX(4.0));
    }

    @Test
    void testFloorIndexInside() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(new double[]{1, 2, 3}, new double[]{1, 4, 9});
        assertEquals(1, f.floorIndexOfX(2.5));
    }

    @Test
    void testFloorIndexSinglePoint() {
        double[] x = {3.0, 6.0, 7.0};
        double[] y = {2.0, 4.0, 5.0};
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(x, y);
        assertEquals(0, f.floorIndexOfX(5.0));
    }

    @Test
    void testFloorIndexOO() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(new double[]{5,6}, new double[]{25,26});
        assertEquals(0, f.floorIndexOfX(4.0));
        assertEquals(0, f.floorIndexOfX(6.0));
    }

    // Тесты интерполяции и экстраполяции

    @Test
    void testInterpolateWithIndex() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(new double[]{0, 2}, new double[]{0, 4});
        assertEquals(2.0, f.interpolate(1.0, 0), 1e-12);
    }

    @Test
    void testInterpolateInvalidIndex() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(new double[]{0, 1}, new double[]{0, 1});
        assertThrows(IndexOutOfBoundsException.class, () -> f.interpolate(0.5, -1));
        assertThrows(IndexOutOfBoundsException.class, () -> f.interpolate(0.5, 1));
    }

    @Test
    void testExtrapolateLeft() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(new double[]{1, 2}, new double[]{1, 4});
        // Наклон = 3, y = 3x - 2 → при x=0: y=-2
        assertEquals(-2.0, f.extrapolateLeft(0.0), 1e-12);
    }

    @Test
    void testExtrapolateRight() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(new double[]{1, 2}, new double[]{1, 4});
        // При x=3: y = 3*3 - 2 = 7
        assertEquals(7.0, f.extrapolateRight(3.0), 1e-12);
    }

    @Test
    void testSinglePointInterpolationExtrapolation() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(new double[]{5,6}, new double[]{25,26});
        assertEquals(30.0, f.interpolate(10.0, 0));
        assertEquals(20.0, f.extrapolateLeft(0.0));
        assertEquals(30.0, f.extrapolateRight(10.0));
    }

    // Тест apply() из AbstractTabulatedFunction (неявно)

    @Test
    void testApplyExactMatch() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(new double[]{1, 2}, new double[]{1, 4});
        assertEquals(1.0, f.apply(1.0));
    }

    @Test
    void testApplyInterpolation() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(new double[]{0, 2}, new double[]{0, 4});
        assertEquals(2.0, f.apply(1.0), 1e-12);
    }

    @Test
    void testApplyExtrapolateLeft() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(new double[]{1, 2}, new double[]{1, 4});
        assertEquals(-2.0, f.apply(0.0), 1e-12);
    }

    @Test
    void testApplyExtrapolateRight() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(new double[]{1, 2}, new double[]{1, 4});
        assertEquals(7.0, f.apply(3.0), 1e-12);
    }

    @Test
    void testRemoveMiddleElement() {
        double[] x = {1.0, 2.0, 3.0, 4.0};
        double[] y = {1.0, 4.0, 9.0, 16.0};
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(x, y);

        f.remove(1); // удаляем x=2.0, y=4.0

        assertEquals(3, f.getCount());
        assertEquals(1.0, f.getX(0));
        assertEquals(3.0, f.getX(1));
        assertEquals(4.0, f.getX(2));
        assertEquals(1.0, f.getY(0));
        assertEquals(9.0, f.getY(1));
        assertEquals(16.0, f.getY(2));
    }

    @Test
    void testRemoveFirstElement() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(
                new double[]{1, 2, 3}, new double[]{1, 4, 9});
        f.remove(0);
        assertEquals(2, f.getCount());
        assertEquals(2.0, f.getX(0));
        assertEquals(3.0, f.getX(1));
    }

    @Test
    void testRemoveLastElement() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(
                new double[]{1, 2, 3}, new double[]{1, 4, 9});
        f.remove(2);
        assertEquals(2, f.getCount());
        assertEquals(1.0, f.getX(0));
        assertEquals(2.0, f.getX(1));
    }

    @Test
    void testRemoveInvalidIndex() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(
                new double[]{1, 2}, new double[]{1, 4});
        assertThrows(IndexOutOfBoundsException.class, () -> f.remove(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> f.remove(2));
        assertThrows(IndexOutOfBoundsException.class, () -> f.remove(5));
    }

    @Test
    void testRemoveLastPointForbidden() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(
                new double[]{5,6}, new double[]{25,3});
        assertThrows(IllegalStateException.class, () -> f.remove(0));
    }

    @Test
    void testApplyAfterRemove() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(
                new double[]{0, 1, 2}, new double[]{0, 1, 4});
        f.remove(1); // осталось [0,2] → y = 2x
        assertEquals(1.0, f.apply(0.5), 1e-12); // 2 * 0.5 = 1.0
    }

    @Test
    void insert_existingX_replacesY() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(new double[]{1.0, 2.0, 3.0}, new double[]{1.0, 4.0, 9.0});
        f.insert(2.0, 100.0);
        assertEquals(3, f.getCount());
        assertEquals(100.0, f.getY(1), 1e-10);
    }

    @Test
    void insert_newX_inMiddle_insertsCorrectly() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(new double[]{1.0, 3.0}, new double[]{1.0, 9.0});
        f.insert(2.0, 4.0);
        assertEquals(3, f.getCount());
        assertEquals(2.0, f.getX(1), 1e-10);
        assertEquals(4.0, f.getY(1), 1e-10);
    }

    @Test
    void insert_newX_atBeginning_insertsCorrectly() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(new double[]{2.0, 3.0}, new double[]{4.0, 9.0});
        f.insert(1.0, 1.0);
        assertEquals(3, f.getCount());
        assertEquals(1.0, f.getX(0), 1e-10);
        assertEquals(1.0, f.getY(0), 1e-10);
    }

    @Test
    void insert_newX_atEnd_insertsCorrectly() {
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(new double[]{1.0, 2.0}, new double[]{1.0, 4.0});
        f.insert(3.0, 9.0);
        assertEquals(3, f.getCount());
        assertEquals(3.0, f.getX(2), 1e-10);
        assertEquals(9.0, f.getY(2), 1e-10);
    }

}