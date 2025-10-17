package concurrent;

import functions.ArrayTabulatedFunction;
import functions.Point;
import org.junit.jupiter.api.Test;
import java.util.Iterator;
import java.util.NoSuchElementException;
import static org.junit.jupiter.api.Assertions.*;

class SynchronizedTabulatedFunctionTest {

    private static final double[] X = {0.0, 1.0, 2.0};
    private static final double[] Y = {0.0, 1.0, 4.0};

    // <<<<>>>> 1. Конструктор и проверка null
    @Test
    void constructor_throwsIllegalArgumentException_whenFunctionIsNull() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new SynchronizedTabulatedFunction(null)
        );
        assertEquals("Function must not be null", ex.getMessage());
    }

    // <<<<>>>> 2. Тесты делегирования методов
    @Test
    void delegatesAllMethodsCorrectly() {
        var original = new ArrayTabulatedFunction(X, Y);
        var sync = new SynchronizedTabulatedFunction(original);

        assertEquals(3, sync.getCount());
        assertEquals(0.0, sync.getX(0));
        assertEquals(4.0, sync.getY(2));
        assertEquals(0, sync.indexOfX(0.0));
        assertEquals(-1, sync.indexOfX(999.0));
        assertEquals(2, sync.indexOfY(4.0));
        assertEquals(0.0, sync.leftBound());
        assertEquals(2.0, sync.rightBound());
        assertEquals(1.0, sync.apply(1.0));

        // Изменяем через sync — должно повлиять на оригинал
        sync.setY(1, 999.0);
        assertEquals(999.0, original.getY(1));
        assertEquals(999.0, sync.getY(1));
    }

    // <<<<>>>> 3. Тест iterator()
    @Test
    void iterator_returnsCopyAndIsSafeFromModification() {
        var original = new ArrayTabulatedFunction(X, Y);
        var sync = new SynchronizedTabulatedFunction(original);

        Iterator<Point> it = sync.iterator();

        // Модифицируем оригинал ПОСЛЕ получения итератора
        original.setY(0, 888.0);
        original.setY(1, 888.0);
        original.setY(2, 888.0);

        // Итератор должен вернуть СТАРЫЕ значения
        assertTrue(it.hasNext());
        Point p1 = it.next();
        assertEquals(0.0, p1.x);
        assertEquals(0.0, p1.y); // не 888!

        assertTrue(it.hasNext());
        Point p2 = it.next();
        assertEquals(1.0, p2.x);
        assertEquals(1.0, p2.y);

        assertTrue(it.hasNext());
        Point p3 = it.next();
        assertEquals(2.0, p3.x);
        assertEquals(4.0, p3.y);

        assertFalse(it.hasNext());
    }

    // <<<<>>>> 4. Тест iterator().next() после окончания
    @Test
    void iterator_throwsNoSuchElementException_whenNextCalledAfterEnd() {
        var sync = new SynchronizedTabulatedFunction(new ArrayTabulatedFunction(X, Y));
        Iterator<Point> it = sync.iterator();

        it.next(); it.next(); it.next(); // 3 элемента
        assertFalse(it.hasNext());
        assertThrows(NoSuchElementException.class, it::next);
    }

    // <<<<>>>> 5. Тест пустого итератора (на всякий случай)
    @Test
    void iterator_worksForTwoPoints() {
        var func = new ArrayTabulatedFunction(new double[]{1.0, 2.0}, new double[]{1.0, 4.0});
        var sync = new SynchronizedTabulatedFunction(func);
        Iterator<Point> it = sync.iterator();

        assertTrue(it.hasNext());
        Point p1 = it.next();
        assertEquals(1.0, p1.x);
        assertEquals(1.0, p1.y);

        assertTrue(it.hasNext());
        Point p2 = it.next();
        assertEquals(2.0, p2.x);
        assertEquals(4.0, p2.y);

        assertFalse(it.hasNext());
    }
}