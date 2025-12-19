package operations;

import functions.MathFunction;

public class LeftSteppingDifferentialOperator extends SteppingDifferentialOperator {

    public LeftSteppingDifferentialOperator(double step1) {
        super(step1);
    }

    public LeftSteppingDifferentialOperator() {
        super();
    }

    @Override
    public MathFunction derive(MathFunction function) {
        return new MathFunction() {
            @Override
            public double apply(double x) {
                // Левая разностная производная:
                // f'(x) ≈ (f(x) - f(x - h)) / h
                double h = step;
                return (function.apply(x) - function.apply(x - h)) / h;
            }
        };
    }
}
