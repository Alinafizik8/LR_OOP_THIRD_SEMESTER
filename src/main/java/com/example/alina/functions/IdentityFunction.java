package com.example.alina.functions;

//просто х
public class IdentityFunction implements MathFunction {
    @Override
    public double apply(double x) {
        return x;
    }
}