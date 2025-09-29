package functions;

// f(x) = х * 2
class DoubleFunction implements MathFunction {
    @Override
    public double apply(double x) {
        return 2 * x;
    }
}