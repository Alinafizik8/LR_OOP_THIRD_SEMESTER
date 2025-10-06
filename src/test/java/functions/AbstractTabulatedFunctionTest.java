package functions;
import exceptions.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AbstractTabulatedFunctionTest {

    //Тесты для checkLengthIsTheSame

    @Test
    void checkLengthIsTheSame_validArrays_noException() {
        double[] x = {1.0, 2.0, 3.0};
        double[] y = {4.0, 5.0, 6.0};

        // Не должно быть исключения
        assertDoesNotThrow(() -> {
            AbstractTabulatedFunction.checkLengthIsTheSame(x, y);
        });
    }

    @Test
    void checkLengthIsTheSame_xLonger_throwsException() {
        double[] x = {1.0, 2.0, 3.0};
        double[] y = {4.0, 5.0};

        DifferentLengthOfArraysException exception = assertThrows(
                DifferentLengthOfArraysException.class,
                () -> AbstractTabulatedFunction.checkLengthIsTheSame(x, y)
        );

        assertEquals("The length of the arrays cannot be different!", exception.getMessage());
    }

    @Test
    void checkLengthIsTheSame_yLonger_throwsException() {
        double[] x = {1.0, 2.0};
        double[] y = {4.0, 5.0, 6.0};

        assertThrows(DifferentLengthOfArraysException.class,
                () -> AbstractTabulatedFunction.checkLengthIsTheSame(x, y));
    }

    @Test
    void checkLengthIsTheSame_oneEmptyArray_throwsException() {
        double[] x = {};
        double[] y = {1.0};

        assertThrows(DifferentLengthOfArraysException.class,
                () -> AbstractTabulatedFunction.checkLengthIsTheSame(x, y));
    }

    @Test
    void checkLengthIsTheSame_bothEmpty_noException() {
        double[] x = {};
        double[] y = {};

        assertDoesNotThrow(() -> AbstractTabulatedFunction.checkLengthIsTheSame(x, y));
    }

    //Тесты для checkSorted

    @Test
    void checkSorted_sortedArray_noException() {
        double[] x = {1.0, 2.0, 3.0, 4.0};

        assertDoesNotThrow(() -> AbstractTabulatedFunction.checkSorted(x));
    }

    @Test
    void checkSorted_singleElement_noException() {
        double[] x = {5.0};

        assertDoesNotThrow(() -> AbstractTabulatedFunction.checkSorted(x));
    }

    @Test
    void checkSorted_unsortedArray_throwsException() {
        double[] x = {1.0, 3.0, 2.0, 4.0};

        ArrayIsNotSortedException exception = assertThrows(
                ArrayIsNotSortedException.class,
                () -> AbstractTabulatedFunction.checkSorted(x)
        );

        assertEquals("The array must be sorted in ascending order!", exception.getMessage());
    }

    @Test
    void checkSorted_equalElements_throwsException() {
        double[] x = {1.0, 2.0, 2.0, 3.0};

        assertThrows(ArrayIsNotSortedException.class,
                () -> AbstractTabulatedFunction.checkSorted(x));
    }

    @Test
    void checkSorted_descendingArray_throwsException() {
        double[] x = {5.0, 4.0, 3.0, 2.0};

        assertThrows(ArrayIsNotSortedException.class,
                () -> AbstractTabulatedFunction.checkSorted(x));
    }

    @Test
    void interpolateStrict_leftXGreaterOrEqualRightX_throwsInterpolationException() {
        // Случай 1: leftX == rightX
        InterpolationException e1 = assertThrows(InterpolationException.class, () ->
                AbstractTabulatedFunction.interpolateStrict(1.0, 1.0, 1.0, 0.0, 1.0)
        );
        assertTrue(e1.getMessage().contains("leftX must be less than rightX"));

        // Случай 2: leftX > rightX
        InterpolationException e2 = assertThrows(InterpolationException.class, () ->
                AbstractTabulatedFunction.interpolateStrict(1.0, 2.0, 1.0, 0.0, 1.0)
        );
        assertTrue(e2.getMessage().contains("leftX must be less than rightX"));
    }
    @Test
    void interpolateStrict_xOutsideInterval_throwsInterpolationException() {
        InterpolationException e = assertThrows(InterpolationException.class, () ->
                AbstractTabulatedFunction.interpolateStrict(3.0, 1.0, 2.0, 1.0, 4.0)
        );
        assertTrue(e.getMessage().contains("outside interpolation interval"));
    }
}