package operations;
import functions.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TabulatedFunctionOperationServiceTest {

    @Test
    void asPoints_returnsCorrectArrayForNormalFunction() {
        double[] x = {1.0, 2.5, 3.7};
        double[] y = {10.0, 25.0, 37.0};
        ArrayTabulatedFunction func = new ArrayTabulatedFunction(x, y);
        Point[] result = TabulatedFunctionOperationService.asPoints(func);
        assertEquals(3, result.length);
        assertEquals(new Point(1.0, 10.0).x, result[0].x);
        assertEquals(new Point(1.0, 10.0).y, result[0].y);
        assertEquals(new Point(2.5, 25.0).x, result[1].x);
        assertEquals(new Point(2.5, 25.0).y, result[1].y);
        assertEquals(new Point(3.7, 37.0).x, result[2].x);
        assertEquals(new Point(3.7, 37.0).y, result[2].y);
    }

    @Test
    void asPoints_throwsNullPointerExceptionWhenInputIsNull() {
        assertThrows(NullPointerException.class, () -> {
            TabulatedFunctionOperationService.asPoints(null);
        });
    }

    @Test
    void asPoints_arrayElementsAreInCorrectOrder() {
        double[] x = {0, 1, 2, 3};
        double[] y = {0, 1, 4, 9}; // y = x^2
        ArrayTabulatedFunction func = new ArrayTabulatedFunction(x, y);
        Point[] result = TabulatedFunctionOperationService.asPoints(func);
        for (int i = 0; i < x.length; i++) {
            assertEquals(x[i], result[i].x, 0.001, "Неверная x-координата в позиции " + i);
            assertEquals(y[i], result[i].y, 0.001, "Неверная y-координата в позиции " + i);
        }
    }

    // Дополнительный тест: проверка, что массив не содержит null
    @Test
    void asPoints_resultArrayContainsNoNulls() {
        double[] x = {1, 2, 3};
        double[] y = {1, 2, 3};
        ArrayTabulatedFunction func = new ArrayTabulatedFunction(x, y);
        Point[] result = TabulatedFunctionOperationService.asPoints(func);
        for (Point p : result) {
            assertNotNull(p);
        }
    }
}