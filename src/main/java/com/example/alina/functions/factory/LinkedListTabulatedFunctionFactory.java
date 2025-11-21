package com.example.alina.functions.factory;

import com.example.alina.functions.LinkedListTabulatedFunction;
import com.example.alina.functions.TabulatedFunction;

public class LinkedListTabulatedFunctionFactory implements TabulatedFunctionFactory {
    @Override
    public TabulatedFunction create(double[] xValues, double[] yValues) {
        return new LinkedListTabulatedFunction(xValues, yValues);
    }
}