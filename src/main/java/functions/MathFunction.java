package functions;

// Интерфейс MathFunction
interface MathFunction {
    double apply(double x);
    CompositeFunction andThen(MathFunction a, MathFunction b){

    }
}