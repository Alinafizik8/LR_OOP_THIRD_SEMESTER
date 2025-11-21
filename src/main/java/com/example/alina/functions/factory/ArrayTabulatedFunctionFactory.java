package com.example.alina.functions.factory;

import com.example.alina.functions.ArrayTabulatedFunction;
import com.example.alina.functions.TabulatedFunction;

public class ArrayTabulatedFunctionFactory implements TabulatedFunctionFactory {
    @Override
    public TabulatedFunction create(double[] xValues, double[] yValues) {
        return new ArrayTabulatedFunction(xValues, yValues);
    }
}