package com.example.alina.functions.factory;
import com.example.alina.functions.*;

public interface TabulatedFunctionFactory {
    TabulatedFunction create(double[] xValues, double[] yValues);
}
