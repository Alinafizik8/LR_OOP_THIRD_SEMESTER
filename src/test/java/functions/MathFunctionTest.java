package functions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MathFunctionTest {
    private static final double DELTA = 1e-9;

    //Вспомогательные функции
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
            return x * 2;
        }
    }

    static class AbsFunction implements MathFunction {
        @Override
        public double apply(double x) { return Math.abs(x); }
    }

    static class SinFunction implements MathFunction {
        @Override
        public double apply(double x) {
            return Math.sin(x);
        }
    }

    @Test
    void testAndThen_simpleChain() {
        // f(x) = x + 1, g(x) = x * 2
        // f.andThen(g) => g(f(x)) = (x + 1) * 2
        MathFunction f = new AddOneFunction();
        MathFunction g = new DoubleFunction();

        MathFunction composite = f.andThen(g);
        assertEquals(6.0, composite.apply(2.0), DELTA); // (2 + 1) * 2 = 6
    }

    @Test
    void testAndThen_threeFunctions() {
        // f(x) = x^2, g(x) = x + 1, h(x) = x * 2
        // f.andThen(g).andThen(h) => h(g(f(x))) = ((x^2) + 1) * 2
        MathFunction f = new SquareFunction();
        MathFunction g = new AddOneFunction();
        MathFunction h = new DoubleFunction();

        MathFunction composite = f.andThen(g).andThen(h);
        assertEquals(10.0, composite.apply(2.0), DELTA); // ((2^2)+1)*2 = (4+1)*2 = 10
    }

    @Test
    void testAndThen_identityEquivalent() {
        MathFunction f = x -> x * 3;
        MathFunction identity = x -> x;

        // f.andThen(identity) == f
        assertEquals(f.apply(5.0), f.andThen(identity).apply(5.0), DELTA);
        // identity.andThen(f) == f
        assertEquals(f.apply(5.0), identity.andThen(f).apply(5.0), DELTA);
    }

    @Test
    void testAndThen_withTrigonometricFunctions() {
        // sin(abs(x))
        MathFunction composite = new AbsFunction().andThen(new SinFunction());
        assertEquals(Math.sin(2.0), composite.apply(-2.0), DELTA); // abs(-2)=2, sin(2)
    }

    @Test
    void testAndThen_nestedComposite() {
        // ((x^2 + 1) * 2)^2
        MathFunction inner = new SquareFunction()
                .andThen(new AddOneFunction())
                .andThen(new DoubleFunction());
        MathFunction full = inner.andThen(new SquareFunction());

        double result = full.apply(1.0); // ((1^2 + 1) * 2)^2 = (2 * 2)^2 = 16
        assertEquals(16.0, result, DELTA);
    }

    @Test
    void testAndThen_nullArgument_throwsException() {
        MathFunction f = x -> x;
        assertThrows(IllegalArgumentException.class, () -> f.andThen(null));
    }

    @Test
    void testAndThen_compositeIsReusable() {
        MathFunction chain = new AddOneFunction().andThen(new SquareFunction());
        assertEquals(9.0, chain.apply(2.0), DELTA);  // (2+1)^2 = 9
        assertEquals(16.0, chain.apply(3.0), DELTA); // (3+1)^2 = 16
    }
}