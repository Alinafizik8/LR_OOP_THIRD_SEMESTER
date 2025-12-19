package functions;

//просто х
@MathFunctionInfo(name = "Тождественная функция (x)", priority = 2)
public class IdentityFunction implements MathFunction {
    @Override
    public double apply(double x) {
        return x;
    }
}