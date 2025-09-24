package functions;

// f(x) = 2 * x
class DoubleFunction implements MathFunction {
    @Override
    public double apply(double x) {
        return 2 * x;
    }
}