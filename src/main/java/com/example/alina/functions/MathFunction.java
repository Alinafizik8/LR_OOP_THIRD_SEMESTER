package com.example.alina.functions;

public interface MathFunction {
    double apply(double x);
    default CompositeFunction andThen(MathFunction afterFunction){
        if (afterFunction == null) {
            throw new IllegalArgumentException("Functions can not be null");
        }
        return new CompositeFunction(this, afterFunction);
    }
}