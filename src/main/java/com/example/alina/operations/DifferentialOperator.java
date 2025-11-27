package com.example.alina.operations;

import com.example.alina.functions.MathFunction;

public interface DifferentialOperator<T extends MathFunction> {
    T derive(T function);
}