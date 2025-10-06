package operations;
import functions.*;
import functions.factory.*;
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

    @Test
    void defaultConstructor_shouldInitializeFactoryWithArrayTabulatedFunctionFactory() {
        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService();
        assertNotNull(service.getFactory());
        // Проверяем, что это именно ArrayTabulatedFunctionFactory
        assertTrue(service.getFactory() instanceof ArrayTabulatedFunctionFactory);
    }

    @Test
    void parameterizedConstructor_shouldSetProvidedFactory() {
        TabulatedFunctionFactory customFactory = new ArrayTabulatedFunctionFactory();
        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService(customFactory);

        assertEquals(customFactory, service.getFactory());
    }

    @Test
    void parameterizedConstructor_shouldThrowExceptionWhenFactoryIsNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            new TabulatedFunctionOperationService((TabulatedFunctionFactory) null);
        });
    }

    @Test
    void setFactory_shouldUpdateFactory() {
        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService();
        TabulatedFunctionFactory newFactory = new ArrayTabulatedFunctionFactory();

        service.setFactory(newFactory);
        assertEquals(newFactory, service.getFactory());
    }

    @Test
    void setFactory_shouldThrowExceptionWhenSettingNull() {
        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService();
        assertThrows(IllegalArgumentException.class, () -> {
            service.setFactory(null);
        });
    }

    @Test
    void asPoints_shouldConvertTabulatedFunctionToArray() {
        double[] x = {1.0, 2.0, 3.0};
        double[] y = {10.0, 20.0, 30.0};
        TabulatedFunctionFactory factory = new ArrayTabulatedFunctionFactory();
        TabulatedFunction func = factory.create(x, y);
        Point[] points = TabulatedFunctionOperationService.asPoints(func);

        assertEquals(3, points.length);
        assertEquals(1.0, points[0].x, 1e-10);
        assertEquals(10.0, points[0].y, 1e-10);
        assertEquals(2.0, points[1].x, 1e-10);
        assertEquals(20.0, points[1].y, 1e-10);
        assertEquals(3.0, points[2].x, 1e-10);
        assertEquals(30.0, points[2].y, 1e-10);
    }

    /*@Test
    void asPoints_shouldReturnEmptyArrayForEmptyFunction() {
        TabulatedFunctionFactory factory = new ArrayTabulatedFunctionFactory();
        TabulatedFunction emptyFunc = factory.create(new double[0], new double[0]);
        Point[] points = TabulatedFunctionOperationService.asPoints(emptyFunc);
        assertEquals(0, points.length);
    }*/

    @Test
    void asPoints_shouldThrowNullPointerExceptionWhenInputIsNull() {
        assertThrows(NullPointerException.class, () -> {
            TabulatedFunctionOperationService.asPoints(null);
        });
    }

}