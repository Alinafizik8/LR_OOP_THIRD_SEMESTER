package functions;

import exceptions.*;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

public class LinkedListTabulatedFunctionTest {

    private static final MathFunction IDENTITY = new IdentityFunction();
    private static final MathFunction SQUARE = new SqrFunction();

    // <<<<<<>>>>>> Конструктор из массивов

    @Test
    void constructorFromArrays_validInput_createsCorrectList() {
        double[] x = {1.0, 2.0, 3.0};
        double[] y = {1.0, 4.0, 9.0};
        LinkedListTabulatedFunction f = new LinkedListTabulatedFunction(x, y);

        assertEquals(3, f.getCount());
        assertEquals(1.0, f.leftBound(), 1e-10);
        assertEquals(3.0, f.rightBound(), 1e-10);
        assertEquals(1.0, f.getX(0), 1e-10);
        assertEquals(4.0, f.getY(1), 1e-10);
        assertEquals(9.0, f.getY(2), 1e-10);
    }

    @Test
    void constructorFromArrays_emptyArrays_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new LinkedListTabulatedFunction(new double[0], new double[0]);
        });
    }

    @Test
    void constructorFromArrays_singlePoint_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new LinkedListTabulatedFunction(new double[]{1.0}, new double[]{1.0});
        });
    }

    @Test
    void constructorFromArrays_unequalLengths_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new LinkedListTabulatedFunction(new double[]{1.0}, new double[]{1.0, 2.0});
        });
    }

    @Test
    void constructorFromArrays_nonStrictlyIncreasingX_throwsException() {
        assertThrows(ArrayIsNotSortedException.class, () -> {
            new LinkedListTabulatedFunction(new double[]{1.0, 1.0, 2.0}, new double[]{1.0, 1.0, 4.0});
        });
        assertThrows(ArrayIsNotSortedException.class, () -> {
            new LinkedListTabulatedFunction(new double[]{1.0, 3.0, 2.0}, new double[]{1.0, 9.0, 4.0});
        });
    }

    //  <<<<<<>>>>>> Конструктор из функции

    @Test
    void constructorFromFunction_multiplePoints_createsCorrectValues() {
        LinkedListTabulatedFunction f = new LinkedListTabulatedFunction(SQUARE, 0.0, 2.0, 3);
        assertEquals(3, f.getCount());
        assertEquals(0.0, f.getX(0), 1e-10);
        assertEquals(1.0, f.getX(1), 1e-10);
        assertEquals(2.0, f.getX(2), 1e-10);
        assertEquals(0.0, f.getY(0), 1e-10);
        assertEquals(1.0, f.getY(1), 1e-10);
        assertEquals(4.0, f.getY(2), 1e-10);
    }

    @Test
    void constructorFromFunction_singlePoint_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new LinkedListTabulatedFunction(IDENTITY, 0.0, 1.0, 1);
        });
    }

    @Test
    void constructorFromFunction_xFromGreaterThanXTo_swapsThem() {
        LinkedListTabulatedFunction f1 = new LinkedListTabulatedFunction(IDENTITY, 1.0, 3.0, 2);
        LinkedListTabulatedFunction f2 = new LinkedListTabulatedFunction(IDENTITY, 3.0, 1.0, 2);
        assertEquals(f1.getX(0), f2.getX(0), 1e-10);
        assertEquals(f1.getX(1), f2.getX(1), 1e-10);
    }

    @Test
    void constructorFromFunction_countLessThanOne_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new LinkedListTabulatedFunction(IDENTITY, 0.0, 1.0, 0);
        });
    }

    // <<<<<<>>>>>> Методы TabulatedFunction

    @Test
    void getX_and_getY_validIndices_returnCorrectValues() {
        double[] x = {0.5, 1.5, 2.5};
        double[] y = {0.25, 2.25, 6.25};
        LinkedListTabulatedFunction f = new LinkedListTabulatedFunction(x, y);

        for (int i = 0; i < 3; i++) {
            assertEquals(x[i], f.getX(i), 1e-10);
            assertEquals(y[i], f.getY(i), 1e-10);
        }
    }

    @Test
    void setY_changesValue() {
        LinkedListTabulatedFunction f = new LinkedListTabulatedFunction(new double[]{1.0, 2.0}, new double[]{1.0, 4.0});
        f.setY(1, 100.0);
        assertEquals(100.0, f.getY(1), 1e-10);
    }

    @Test
    void indexOfX_exactMatch_returnsIndex() {
        LinkedListTabulatedFunction f = new LinkedListTabulatedFunction(new double[]{1.0, 2.0, 3.0}, new double[]{1.0, 4.0, 9.0});
        assertEquals(1, f.indexOfX(2.0));
    }

    @Test
    void indexOfX_noMatch_returnsMinusOne() {
        LinkedListTabulatedFunction f = new LinkedListTabulatedFunction(new double[]{1.0, 2.0}, new double[]{1.0, 4.0});
        assertEquals(-1, f.indexOfX(1.5));
    }

    @Test
    void indexOfY_exactMatch_returnsIndex() {
        LinkedListTabulatedFunction f = new LinkedListTabulatedFunction(new double[]{1.0, 2.0}, new double[]{10.0, 20.0});
        assertEquals(1, f.indexOfY(20.0));
    }

    @Test
    void indexOfY_noMatch_returnsMinusOne() {
        LinkedListTabulatedFunction f = new LinkedListTabulatedFunction(new double[]{1.0, 2.0}, new double[]{10.0, 20.0});
        assertEquals(-1, f.indexOfY(15.0));
    }

    // <<<<<<>>>>>> floorIndexOfX

    @Test
    void floorIndexOfX_xLessThanLeftBound_returnsZero() {
        LinkedListTabulatedFunction f = new LinkedListTabulatedFunction(new double[]{1.0, 2.0, 3.0}, new double[]{1.0, 4.0, 9.0});
        assertEquals(0, f.floorIndexOfX(0.5));
    }

    @Test
    void floorIndexOfX_xGreaterThanRightBound_returnsCount() {
        LinkedListTabulatedFunction f = new LinkedListTabulatedFunction(new double[]{1.0, 2.0, 3.0}, new double[]{1.0, 4.0, 9.0});
        assertEquals(3, f.floorIndexOfX(3.5)); // count = 3
    }

    @Test
    void floorIndexOfX_xEqualsRightBound_returnsLastIndex() {
        LinkedListTabulatedFunction f = new LinkedListTabulatedFunction(new double[]{1.0, 2.0, 3.0}, new double[]{1.0, 4.0, 9.0});
        assertEquals(2, f.floorIndexOfX(3.0));
    }

    @Test
    void floorIndexOfX_insideInterval_returnsCorrectIndex() {
        LinkedListTabulatedFunction f = new LinkedListTabulatedFunction(new double[]{0.0, 1.0, 2.0, 3.0}, new double[]{0.0, 1.0, 4.0, 9.0});
        assertEquals(1, f.floorIndexOfX(1.7)); // между 1.0 и 2.0 → индекс 1
    }

    // <<<<<<>>>>>> Интерполяция и экстраполяция

    @Test
    void interpolate_linearInterpolation_works() {
        LinkedListTabulatedFunction f = new LinkedListTabulatedFunction(new double[]{0.0, 2.0}, new double[]{0.0, 4.0});
        double result = f.interpolate(1.0, 0);
        assertEquals(2.0, result, 1e-10);
    }

    @Test
    void extrapolateLeft_linearExtrapolation_works() {
        LinkedListTabulatedFunction f = new LinkedListTabulatedFunction(new double[]{1.0, 2.0}, new double[]{1.0, 4.0});
        double result = f.extrapolateLeft(0.0); // y = 3x - 2 → y(0) = -2
        assertEquals(-2.0, result, 1e-10);
    }

    @Test
    void extrapolateRight_linearExtrapolation_works() {
        LinkedListTabulatedFunction f = new LinkedListTabulatedFunction(new double[]{1.0, 2.0}, new double[]{1.0, 4.0});
        double result = f.extrapolateRight(3.0); // y = 3x - 2 → y(3) = 7
        assertEquals(7.0, result, 1e-10);
    }

    // <<<<<<>>>>>> apply() (включая X*)

    @Test
    void apply_exactX_returnsY() {
        LinkedListTabulatedFunction f = new LinkedListTabulatedFunction(new double[]{1.0, 2.0, 3.0}, new double[]{1.0, 4.0, 9.0});
        assertEquals(4.0, f.apply(2.0), 1e-10);
    }

    @Test
    void apply_insideInterval_interpolates() {
        LinkedListTabulatedFunction f = new LinkedListTabulatedFunction(new double[]{0.0, 2.0}, new double[]{0.0, 4.0});
        assertEquals(2.0, f.apply(1.0), 1e-10);
    }

    @Test
    void apply_leftOfInterval_extrapolatesLeft() {
        LinkedListTabulatedFunction f = new LinkedListTabulatedFunction(new double[]{1.0, 2.0}, new double[]{1.0, 4.0});
        assertEquals(-2.0, f.apply(0.0), 1e-10);
    }

    @Test
    void apply_rightOfInterval_extrapolatesRight() {
        LinkedListTabulatedFunction f = new LinkedListTabulatedFunction(new double[]{1.0, 2.0}, new double[]{1.0, 4.0});
        assertEquals(7.0, f.apply(3.0), 1e-10);
    }

    // <<<<<<>>>>>> Внутренняя структура

    @Test
    void cyclicList_headPrevIsLastNode() {
        double[] x = {1.0, 2.0, 3.0};
        double[] y = {1.0, 4.0, 9.0};
        LinkedListTabulatedFunction f = new LinkedListTabulatedFunction(x, y);
        assertEquals(3.0, f.rightBound(), 1e-10);
        assertEquals(1.0, f.leftBound(), 1e-10);
    }

    @Test
    void getNode_optimizedAccess_worksFromBothEnds() {
        double[] x = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        double[] y = new double[10];
        for (int i = 0; i < 10; i++) y[i] = i * i;
        LinkedListTabulatedFunction f = new LinkedListTabulatedFunction(x, y);

        assertEquals(81.0, f.getY(9), 1e-10);
        assertEquals(0.0, f.getY(0), 1e-10);
    }

    // <<<<<<>>>>>> Insert

    @Test
    void testEmptyList() {
        assertThrows(IllegalArgumentException.class, () -> {
            LinkedListTabulatedFunction f = new LinkedListTabulatedFunction(new double[]{}, new double[]{});
        });
    }

    @Test
    void testInsertReplacesYIfExists() {
        LinkedListTabulatedFunction f = new LinkedListTabulatedFunction(
                new double[]{1.0, 2.0}, new double[]{1.0, 4.0});
        f.insert(1.0, 100.0);
        assertEquals(100.0, f.getY(0));
        assertEquals(2, f.getCount());
    }

    @Test
    void testInsertAtBeginning() {
        LinkedListTabulatedFunction f = new LinkedListTabulatedFunction(
                new double[]{2.0, 3.0}, new double[]{4.0, 9.0});
        f.insert(1.0, 1.0);
        assertEquals(1.0, f.getX(0));
        assertEquals(2.0, f.getX(1));
        assertEquals(3.0, f.getX(2));
        assertEquals(1.0, f.getY(0));
    }

    @Test
    void testInsertAtEnd() {
        LinkedListTabulatedFunction f = new LinkedListTabulatedFunction(
                new double[]{1.0, 2.0}, new double[]{1.0, 4.0});
        f.insert(3.0, 9.0);
        assertEquals(3, f.getCount());
        assertEquals(3.0, f.getX(2));
        assertEquals(9.0, f.getY(2));
    }

    @Test
    void testInsertInMiddle() {
        LinkedListTabulatedFunction f = new LinkedListTabulatedFunction(
                new double[]{1.0, 3.0}, new double[]{1.0, 9.0});
        f.insert(2.0, 4.0);
        assertEquals(3, f.getCount());
        assertEquals(1.0, f.getX(0));
        assertEquals(2.0, f.getX(1));
        assertEquals(3.0, f.getX(2));
        assertEquals(4.0, f.getY(1));
    }

    @Test
    void testInsertMaintainsOrder() {
        LinkedListTabulatedFunction f = new LinkedListTabulatedFunction(
                new double[]{1.0, 4.0}, new double[]{1.0, 16.0});
        f.insert(2.0, 4.0);
        f.insert(3.0, 9.0);
        f.insert(0.5, 0.25);
        f.insert(5.0, 25.0);

        double[] expectedX = {0.5, 1.0, 2.0, 3.0, 4.0, 5.0};
        for (int i = 0; i < expectedX.length; i++) {
            assertEquals(expectedX[i], f.getX(i), 1e-10);
        }
    }

    // <<<<<<>>>>>> Remove

    @Test
    void remove_middleElement_updatesList() {
        LinkedListTabulatedFunction f = new LinkedListTabulatedFunction(
                new double[]{1.0, 2.0, 3.0}, new double[]{1.0, 4.0, 9.0}
        );
        f.remove(1);
        assertEquals(2, f.getCount());
        assertEquals(1.0, f.getX(0), 1e-10);
        assertEquals(3.0, f.getX(1), 1e-10);
        assertEquals(9.0, f.getY(1), 1e-10);
    }

    @Test
    void remove_firstElement_updatesHead() {
        LinkedListTabulatedFunction f = new LinkedListTabulatedFunction(
                new double[]{1.0, 2.0, 3.0}, new double[]{1.0, 4.0, 9.0}
        );
        f.remove(0);
        assertEquals(2, f.getCount());
        assertEquals(2.0, f.leftBound(), 1e-10);
        assertEquals(4.0, f.getY(0), 1e-10);
    }

    @Test
    void remove_lastElement_updatesTail() {
        LinkedListTabulatedFunction f = new LinkedListTabulatedFunction(
                new double[]{1.0, 2.0, 3.0}, new double[]{1.0, 4.0, 9.0}
        );
        f.remove(2);
        assertEquals(2, f.getCount());
        assertEquals(2.0, f.rightBound(), 1e-10);
        assertEquals(4.0, f.getY(1), 1e-10);
    }

    @Test
    void remove_onlyElement_makesListEmpty() {
        LinkedListTabulatedFunction f = new LinkedListTabulatedFunction(
                new double[]{5.0, 6.0}, new double[]{25.0, 26.0}
        );
        f.remove(0);
        assertEquals(1, f.getCount());
        f.remove(0);
        assertEquals(0, f.getCount());
        // head == null, дальнейшие вызовы недопустимы
    }

    @Test
    void remove_invalidIndex_throwsException() {
        // Создаём корректную функцию из 2 точек (минимум по требованиям ЛР №3)
        LinkedListTabulatedFunction f = new LinkedListTabulatedFunction(
                new double[]{1.0, 2.0},
                new double[]{1.0, 4.0}
        );

        // Попытка удалить индекс -1 должно выбросить исключение
        assertThrows(IndexOutOfBoundsException.class, () -> f.remove(-1));

        // Попытка удалить индекс 2 (равен count) тоже недопустимо
        assertThrows(IndexOutOfBoundsException.class, () -> f.remove(2));
    }

    @Test
    void constructorFromFunction_xFromEqualsXTo_createsConstantFunction() {
        LinkedListTabulatedFunction f = new LinkedListTabulatedFunction(IDENTITY, 5.0, 5.0, 3);
        assertEquals(3, f.getCount());
        for (int i = 0; i < 3; i++) {
            assertEquals(5.0, f.getX(i), 1e-10);
            assertEquals(5.0, f.getY(i), 1e-10);
        }
    }

    @Test
    void getX_invalidIndex_throwsIndexOutOfBoundsException() {
        LinkedListTabulatedFunction f = new LinkedListTabulatedFunction(
                new double[]{1.0, 2.0}, new double[]{1.0, 4.0}
        );
        assertThrows(IndexOutOfBoundsException.class, () -> f.getX(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> f.getX(2));
    }

    @Test
    void insert_intoEmptyListAfterRemoval_usesHeadNullBranch() {
        // Создаём функцию из 2 точек (минимум по требованиям ЛР №3)
        LinkedListTabulatedFunction f = new LinkedListTabulatedFunction(
                new double[]{1.0, 2.0},
                new double[]{1.0, 4.0}
        );

        // Удаляем обе точки → список становится пустым
        f.remove(0); // остаётся 1 точка
        f.remove(0); // head == null

        // Теперь вставляем в пустой список
        f.insert(5.0, 25.0);

        // Проверяем, что вставка прошла успешно
        assertEquals(1, f.getCount());
        assertEquals(5.0, f.getX(0), 1e-10);
        assertEquals(25.0, f.getY(0), 1e-10);
    }

    @Test
    void iterator_whileLoop_worksCorrectly() {
        LinkedListTabulatedFunction f = new LinkedListTabulatedFunction(
                new double[]{1.0, 2.0, 3.0},
                new double[]{1.0, 4.0, 9.0}
        );

        Iterator<Point> iterator = f.iterator();
        int i = 0;
        double[] expectedX = {1.0, 2.0, 3.0};
        double[] expectedY = {1.0, 4.0, 9.0};

        while (iterator.hasNext()) {
            Point point = iterator.next();
            assertEquals(expectedX[i], point.x, 1e-10);
            assertEquals(expectedY[i], point.y, 1e-10);
            i++;
        }
        assertEquals(3, i);
    }

    @Test
    void iterator_forEachLoop_worksCorrectly() {
        LinkedListTabulatedFunction f = new LinkedListTabulatedFunction(
                new double[]{0.0, 1.0, 2.0},
                new double[]{0.0, 1.0, 4.0}
        );

        double[] expectedX = {0.0, 1.0, 2.0};
        double[] expectedY = {0.0, 1.0, 4.0};
        int i = 0;

        for (Point point : f) {
            assertEquals(expectedX[i], point.x, 1e-10);
            assertEquals(expectedY[i], point.y, 1e-10);
            i++;
        }
        assertEquals(3, i);
    }

    @Test
    void iterator_emptyList_throwsNoSuchElementException() {
        // Создаём из 2 точек и удаляем обе
        LinkedListTabulatedFunction f = new LinkedListTabulatedFunction(
                new double[]{1.0, 2.0}, new double[]{1.0, 4.0}
        );
        f.remove(0);
        f.remove(0); // теперь head == null

        Iterator<Point> it = f.iterator();
        assertFalse(it.hasNext());
        assertThrows(NoSuchElementException.class, it::next);
    }

}