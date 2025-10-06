package functions;

import java.util.function.Function;

public interface MathFunction {
    double apply(double x);
    default CompositeFunction andThen(MathFunction afterFunction){
        if (afterFunction == null) {
            throw new IllegalArgumentException("Функции не могyт быть null");
        }
        return new CompositeFunction(this, afterFunction);
    }
}