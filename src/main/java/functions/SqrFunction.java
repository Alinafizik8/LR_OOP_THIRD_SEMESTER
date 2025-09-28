package functions;

//return x^2
class SqrFunction implements MathFunction {
    private double digit;
    @Override
    public double apply(double x){
        return java.lang.Math.pow(x,2);
    }
}