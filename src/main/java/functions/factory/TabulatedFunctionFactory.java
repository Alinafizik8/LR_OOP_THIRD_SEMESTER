package functions.factory;
import functions.*;

public interface TabulatedFunctionFactory {
    TabulatedFunction create(double[] xValues, double[] yValues);

    default TabulatedFunction create(MathFunction source, double xFrom, double xTo, int count) {
        throw new UnsupportedOperationException("This factory does not support creating from MathFunction");
    }
}
