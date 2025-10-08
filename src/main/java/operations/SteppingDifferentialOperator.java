package operations;

import functions.MathFunction;

public abstract class SteppingDifferentialOperator implements DifferentialOperator<MathFunction> {

    protected double step;

    public SteppingDifferentialOperator(double step1) {
        if (Double.isInfinite(step) || (step<=0) || (Double.isNaN(step))) {
            throw new IllegalArgumentException("Invalid argument value");
        }
        this.step = step1;
    }

    public double getStep() {
        return step;
    }

    public void setStep(double step2) {
        if ((step <= 0.0) || Double.isInfinite(step) || Double.isNaN(step)) {
            throw new IllegalArgumentException("Invalid argument value");
        }
        this.step = step2;
    }
}
