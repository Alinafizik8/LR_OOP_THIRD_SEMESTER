package operations;

import functions.MathFunction;

public abstract class SteppingDifferentialOperator implements DifferentialOperator<MathFunction> {

    protected double step;

    public SteppingDifferentialOperator(double step1) {
        if (Double.isNaN(step1) || Double.isInfinite(step1) || step1 <= 0) {
            throw new IllegalArgumentException();
        }
        this.step = step1;
    }

    public SteppingDifferentialOperator() {

    }

    public double getStep() {
        return step;
    }

    public void setStep(double step2) {
        if ((step2 <= 0.0) || Double.isInfinite(step2) || Double.isNaN(step2)) {
            throw new IllegalArgumentException("Invalid argument value");
        }
        this.step = step2;
    }
}
