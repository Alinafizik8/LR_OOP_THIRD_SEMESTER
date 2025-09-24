package functions;

// f(x) = x + 1
class AddOneFunction implements MathFunction {
    @Override
    public double apply(double x) {
        return x + 1;
    }
}