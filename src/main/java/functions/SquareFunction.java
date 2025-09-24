package functions;

// f(x) = x^2
class SquareFunction implements MathFunction {
    @Override
    public double apply(double x) {
        return x * x;
    }
}