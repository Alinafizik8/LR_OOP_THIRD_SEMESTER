package Functions;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CompositeFunctionTest {

    // Вспомогательные функции
    static class SquareFunction implements MathFunction {
        @Override
        public double apply(double x) {
            return x * x;
        }
    }

    static class AddOneFunction implements MathFunction {
        @Override
        public double apply(double x) {
            return x + 1;
        }
    }

    static class DoubleFunction implements MathFunction {
        @Override
        public double apply(double x) {
            return 2 * x;
        }
    }

    @Test
    void testSimpleComposition_SquareThenAddOne() {
        MathFunction square = new SquareFunction();
        MathFunction addOne = new AddOneFunction();

        // h(x) = addOne(square(x)) = x^2 + 1
        CompositeFunction h = new CompositeFunction(square, addOne);

        assertEquals(1.0, h.apply(0.0), 1e-12);   // 0^2 + 1 = 1
        assertEquals(2.0, h.apply(1.0), 1e-12);   // 1^2 + 1 = 2
        assertEquals(5.0, h.apply(2.0), 1e-12);   // 4 + 1 = 5
        assertEquals(2.0, h.apply(-1.0), 1e-12);  // (-1)^2 + 1 = 2
    }

    @Test
    void testSimpleComposition_AddOneThenSquare() {
        MathFunction addOne = new AddOneFunction();
        MathFunction square = new SquareFunction();

        // h(x) = square(addOne(x)) = (x + 1)^2
        CompositeFunction h = new CompositeFunction(addOne, square);

        assertEquals(1.0, h.apply(0.0), 1e-12);   // (0+1)^2 = 1
        assertEquals(4.0, h.apply(1.0), 1e-12);   // (1+1)^2 = 4
        assertEquals(9.0, h.apply(2.0), 1e-12);   // (2+1)^2 = 9
        assertEquals(0.0, h.apply(-1.0), 1e-12);  // (-1+1)^2 = 0
    }

    @Test
    void testCompositionWithSameFunction() {
        MathFunction doubleFunc = new DoubleFunction();

        // h(x) = double(double(x)) = 2*(2*x) = 4x
        CompositeFunction h = new CompositeFunction(doubleFunc, doubleFunc);

        assertEquals(0.0, h.apply(0.0), 1e-12);
        assertEquals(4.0, h.apply(1.0), 1e-12);
        assertEquals(8.0, h.apply(2.0), 1e-12);
        assertEquals(-4.0, h.apply(-1.0), 1e-12);
    }

    @Test
    void testComplexComposition_CompositeOfComposite() {
        MathFunction square = new SquareFunction();
        MathFunction addOne = new AddOneFunction();

        // f(x) = square(addOne(x)) = (x+1)^2
        CompositeFunction f = new CompositeFunction(addOne, square);

        // g(x) = addOne(square(x)) = x^2 + 1
        CompositeFunction g = new CompositeFunction(square, addOne);

        // h(x) = g(f(x)) = g((x+1)^2) = ((x+1)^2)^2 + 1 = (x+1)^4 + 1
        CompositeFunction h = new CompositeFunction(f, g);

        // Проверим x = 1: (1+1)^4 + 1 = 16 + 1 = 17
        assertEquals(17.0, h.apply(1.0), 1e-12);

        // x = 0: (0+1)^4 + 1 = 1 + 1 = 2
        assertEquals(2.0, h.apply(0.0), 1e-12);

        // x = -1: (0)^4 + 1 = 1
        assertEquals(1.0, h.apply(-1.0), 1e-12);
    }

    @Test
    void testDeepNesting() {
        MathFunction addOne = new AddOneFunction();

        // ((((x + 1) + 1) + 1) + 1) = x + 4
        CompositeFunction h1 = new CompositeFunction(addOne, addOne);        // x+2
        CompositeFunction h2 = new CompositeFunction(h1, addOne);           // x+3
        CompositeFunction h3 = new CompositeFunction(h2, addOne);           // x+4

        assertEquals(4.0, h3.apply(0.0), 1e-12);
        assertEquals(5.0, h3.apply(1.0), 1e-12);
        assertEquals(0.0, h3.apply(-4.0), 1e-12);
    }

    @Test
    void testNullArguments_ThrowsException() {
        MathFunction addOne = new AddOneFunction();

        assertThrows(IllegalArgumentException.class, () -> {
            new CompositeFunction(null, addOne);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new CompositeFunction(addOne, null);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new CompositeFunction(null, null);
        });
    }

    @Test
    void testCompositionWithRungeKuttaFunction() {
        // dy/dx = -y, y(0)=1 → y(x) = exp(-x)
        RungeKuttaFunction.MathFunction2D decay = (x, y) -> -y;
        MathFunction expNeg = new RungeKuttaFunction(0.0, 1.0, decay, 0.001);

        // f(x) = exp(-x)
        // g(x) = x^2
        MathFunction square = new SquareFunction();

        // h(x) = (exp(-x))^2 = exp(-2x)
        CompositeFunction h = new CompositeFunction(expNeg, square);

        double x = 1.0;
        double expected = Math.exp(-2 * x); // ≈ 0.1353
        double actual = h.apply(x);

        assertEquals(expected, actual, 1e-4, "Композиция с RungeKuttaFunction должна работать");
    }
}