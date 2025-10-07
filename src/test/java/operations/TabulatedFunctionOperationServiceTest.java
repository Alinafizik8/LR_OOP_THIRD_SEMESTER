package operations;

import exceptions.InconsistentFunctionsException;
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

    @Test
    void asPoints_shouldThrowNullPointerExceptionWhenInputIsNull() {
        assertThrows(NullPointerException.class, () -> {
            TabulatedFunctionOperationService.asPoints(null);
        });
    }

    // Корректность умножения
    @Test
    void multiply_twoFunctions_returnsCorrectResult() {
        double[] x = {0.0, 1.0, 2.0};
        double[] y1 = {1.0, 2.0, 3.0}; // f(x) = x + 1
        double[] y2 = {0.0, 1.0, 4.0}; // g(x) = x^2
        TabulatedFunction f = new ArrayTabulatedFunction(x, y1);
        TabulatedFunction g = new LinkedListTabulatedFunction(x, y2);

        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService();
        TabulatedFunction product = service.multiply(f, g);

        assertEquals(3, product.getCount());
        assertEquals(0.0, product.getY(0), 1e-10); // (1 * 0) = 0
        assertEquals(2.0, product.getY(1), 1e-10); // (2 * 1) = 2
        assertEquals(12.0, product.getY(2), 1e-10); // (3 * 4) = 12
    }

    // Корректность деления
    @Test
    void divide_twoFunctions_returnsCorrectResult() {
        double[] x = {1.0, 2.0, 4.0};
        double[] y1 = {2.0, 4.0, 8.0}; // f(x) = 2x
        double[] y2 = {1.0, 2.0, 4.0}; // g(x) = x
        TabulatedFunction f = new ArrayTabulatedFunction(x, y1);
        TabulatedFunction g = new LinkedListTabulatedFunction(x, y2);

        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService();
        TabulatedFunction quotient = service.divide(f, g);

        assertEquals(3, quotient.getCount());
        assertEquals(2.0, quotient.getY(0), 1e-10); // 2 / 1 = 2
        assertEquals(2.0, quotient.getY(1), 1e-10); // 4 / 2 = 2
        assertEquals(2.0, quotient.getY(2), 1e-10); // 8 / 4 = 2
    }

    //Обработка деления на ноль
    @Test
    void divide_byZero_throwsArithmeticException() {
        double[] x = {1.0, 2.0};
        double[] y1 = {1.0, 2.0};
        double[] y2 = {1.0, 0.0}; // деление на 0 во второй точке
        TabulatedFunction f = new ArrayTabulatedFunction(x, y1);
        TabulatedFunction g = new LinkedListTabulatedFunction(x, y2);

        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService();
        assertThrows(ArithmeticException.class, () -> service.divide(f, g));
    }

    //Проверка согласованности функций (одинаковые x)
    @Test
    void multiply_inconsistentFunctions_throwsInconsistentFunctionsException() {
        TabulatedFunction f = new ArrayTabulatedFunction(new double[]{0, 1}, new double[]{0, 1});
        TabulatedFunction g = new ArrayTabulatedFunction(new double[]{0, 2}, new double[]{0, 4}); // разные x

        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService();
        assertThrows(InconsistentFunctionsException.class, () -> service.multiply(f, g));
    }

}