package operations;

import functions.MathFunction;

public class RightSteppingDifferentialOperator extends SteppingDifferentialOperator {

    private RightSteppingDifferentialOperator(double step1) {
        super(step1);
    }

    @Override
    public MathFunction derive(MathFunction function) {
        return new MathFunction() {
            @Override
            public double apply(double x) {
                // Правая разностная производная:
                // f'(x) ≈ (f(x) - f(x - h)) / h
                double h = step;
                return (function.apply(x) - function.apply(x + h)) / h;
            }
        };
    }
}

