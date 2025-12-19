package functions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CombinedTabulatedFunctionsTest {

    //Простая аналитическая функция: f(x) = x
    private static final MathFunction IDENTITY = x -> x;

    // Аналитическая функция: f(x) = x^2
    private static final MathFunction SQUARE = x -> x * x;

    @Test
    public void testSumOfArrayAndListTabulatedFunctions() {
        // f(x) = x на [0, 2] с 3 точками → (0,0), (1,1), (2,2)
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(IDENTITY, 0.0, 2.0, 3);
        ArrayTabulatedFunction g = new ArrayTabulatedFunction(IDENTITY, 0.0, 2.0, 3);

        MathFunction sum = Functions.sum(f, g); // f(x) + g(x) = 2x

        assertEquals(0.0, sum.apply(0.0), 1e-12);
        assertEquals(2.0, sum.apply(1.0), 1e-12);
        assertEquals(4.0, sum.apply(2.0), 1e-12);
        // Интерполяция в середине
        assertEquals(1.0, sum.apply(0.5), 1e-12); // 2 * 0.5 = 1.0
    }

    @Test
    public void testMultiplyTabulatedAndAnalytic() {
        // f(x) = x (табулированная)
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(IDENTITY, 1.0, 3.0, 3);
        // g(x) = x (аналитическая)
        MathFunction product = Functions.multiply(f, IDENTITY); // x * x = x^2

        assertEquals(1.0, product.apply(1.0), 1e-12);
        assertEquals(4.0, product.apply(2.0), 1e-12);
        assertEquals(9.0, product.apply(3.0), 1e-12);
        // Интерполяция
        assertEquals(2.25, product.apply(1.5), 1e-12); // (1.5)^2 = 2.25
    }

    @Test
    public void testComposeTwoTabulatedFunctions() {
        // Внутренняя: h(x) = x^2 на [0, 2]
        ArrayTabulatedFunction inner = new ArrayTabulatedFunction(SQUARE, 0.0, 2.0, 5);
        // Внешняя: f(y) = y на [0, 4] (так как max(x^2)=4)
        ArrayTabulatedFunction outer = new  ArrayTabulatedFunction(IDENTITY, 0.0, 4.0, 5);

        MathFunction composed = Functions.compose(outer, inner); // f(h(x)) = x^2

        assertEquals(0.0, composed.apply(0.0), 1e-12);
        assertEquals(1.0, composed.apply(1.0), 1e-12);
        assertEquals(4.0, composed.apply(2.0), 1e-12);
        // Интерполяция во внутренней и внешней функциях
        assertEquals(2.25, composed.apply(1.5), 1e-12); // (1.5)^2 = 2.25
    }

    @Test
    public void testExtrapolationInCombinedFunction() {
        // f(x) = x на [1, 2]
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(IDENTITY, 1.0, 2.0, 2);
        MathFunction g = Functions.multiply(f, f); // f(x)^2

        // Экстраполяция слева: x=0 → f(0) = 2*0 - 1 = -1 (линейная экстраполяция через (1,1),(2,2))
        // Тогда g(0) = (-1)^2 = 1
        assertEquals(1.0, g.apply(0.0), 10);

        // Экстраполяция справа: x=3 → f(3) = 3 → g(3) = 9
        assertEquals(9.0, g.apply(3.0), 1e-12);
    }

    @Test
    public void testDivisionByZeroInTabulatedFunctions() {
        ArrayTabulatedFunction numerator = new ArrayTabulatedFunction(x -> 1.0, 0.0, 1.0, 2);
        ArrayTabulatedFunction denominator = new ArrayTabulatedFunction(x -> 0.0, 0.0, 1.0, 2);

        MathFunction division = x -> numerator.apply(x) / denominator.apply(x);

        // Деление на ноль → Infinity
        assertTrue(Double.isInfinite(division.apply(0.5)));
    }

    @Test
    public void testCombinationWithDifferentDomains() {
        // f(x) = x на [0, 1]
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(IDENTITY, 0.0, 1.0, 2);
        // g(x) = x на [1, 2]
        ArrayTabulatedFunction g = new ArrayTabulatedFunction(IDENTITY, 1.0, 2.0, 2);

        MathFunction sum = Functions.sum(f, g);

        // В точке x=0.5: f(0.5)=0.5, g(0.5) — экстраполяция слева → g(0.5) = 2*0.5 - 1 = 0
        // Но по логике экстраполяции g: через (1,1),(2,2) → g(x) = x → g(0.5)=0.5
        // Однако в нашем случае экстраполяция линейна → g(0.5) = 0.5
        assertEquals(1.0, sum.apply(0.5), 1e-12); // 0.5 + 0.5

        // В точке x=1.5: f(1.5) — экстраполяция справа = 1.5, g(1.5)=1.5 → сумма=3.0
        assertEquals(3.0, sum.apply(1.5), 1e-12);
    }
}