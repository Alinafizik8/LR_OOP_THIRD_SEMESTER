package functions;

public class Functions {
    public static MathFunction sum(MathFunction f, MathFunction g) {
        return x -> f.apply(x) + g.apply(x);
    }

    public static MathFunction multiply(MathFunction f, MathFunction g) {
        return x -> f.apply(x) * g.apply(x);
    }

    public static MathFunction compose(MathFunction outer, MathFunction inner) {
        return x -> outer.apply(inner.apply(x));
    }

    public static MathFunction pow(MathFunction f, double exponent) {
        return x -> Math.pow(f.apply(x), exponent);
    }
}