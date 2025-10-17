package functions;

public class ConstantFunction implements MathFunction {

    private final double const_digit;

    //конструктор с константой в аргументе
    public ConstantFunction(double const_digit) {
        this.const_digit = const_digit;
    }

    public double getT(){
        return const_digit;
    }

    @Override
    public double apply(double x) {
        return const_digit;
    }
}

