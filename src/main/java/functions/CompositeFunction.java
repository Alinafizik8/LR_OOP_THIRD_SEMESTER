package functions;

class CompositeFunction implements MathFunction {

    private final MathFunction firstFunction;   // f(x)
    private final MathFunction secondFunction;  // g(x)

    /**
     * Конструктор композиции: h(x) = g(f(x))
     *
     * firstFunction  функция f, применяемая первой
     * secondFunction функция g, применяемая второй
     */
    public CompositeFunction(MathFunction firstFunction, MathFunction secondFunction) {
        if (firstFunction == null || secondFunction == null) {
            throw new IllegalArgumentException("Функции не могут быть null");
        }
        this.firstFunction = firstFunction;
        this.secondFunction = secondFunction;
    }

    @Override
    public double apply(double x) {
        double intermediate = firstFunction.apply(x);
        return secondFunction.apply(intermediate);
    }
}