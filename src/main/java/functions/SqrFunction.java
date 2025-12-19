package functions;

//return x*х
@MathFunctionInfo(name = "Квадратичная функция (x²)", priority = 1)
public class SqrFunction implements MathFunction {
    private double digit;
    @Override
    public double apply(double x){
        return java.lang.Math.pow(x,2);
    }
}