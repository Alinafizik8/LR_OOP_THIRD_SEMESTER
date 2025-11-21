package com.example.alina.operations;

import com.example.alina.concurrent.SynchronizedTabulatedFunction;
import com.example.alina.functions.*;
import com.example.alina.functions.factory.TabulatedFunctionFactory;
import com.example.alina.functions.factory.ArrayTabulatedFunctionFactory;

public class TabulatedDifferentialOperator implements DifferentialOperator<TabulatedFunction> {

    private TabulatedFunctionFactory factory;

    // Конструктор без аргументов — по умолчанию ArrayTabulatedFunctionFactory
    public TabulatedDifferentialOperator() {
        this.factory = new ArrayTabulatedFunctionFactory();
    }

    // Конструктор с фабрикой
    public TabulatedDifferentialOperator(TabulatedFunctionFactory factory) {
        this.factory = factory;
    }

    // Геттер и сеттер для фабрики
    public TabulatedFunctionFactory getFactory() {
        return factory;
    }

    public void setFactory(TabulatedFunctionFactory factory) {
        this.factory = factory;
    }

    @Override
    public TabulatedFunction derive(TabulatedFunction function) {
        if (function == null) {
            throw new IllegalArgumentException("Function must not be null");
        }

        Point[] points = TabulatedFunctionOperationService.asPoints(function);
        int n = points.length;

        double[] xValues = new double[n];
        double[] yValues = new double[n];

        // Копируем x (они не меняются)
        for (int i = 0; i < n; i++) {
            xValues[i] = points[i].x;
        }

        /* Численное дифференцирование:
         - первая точка: правая разностная производная
         - последняя точка: левая разностная производная
         - остальные: центральная разностная производная
        */
        // Первая точка (правая производная)
        yValues[0] = (points[1].y - points[0].y) / (points[1].x - points[0].x);

        // Внутренние точки (центральная производная)
        for (int i = 1; i < n - 1; i++) {
            // Простая центральная разность
            yValues[i] = (points[i + 1].y - points[i - 1].y) / (points[i + 1].x - points[i - 1].x);
        }

        // Последняя точка (левая производная, т.е. значение такое же, как и предпоследнее)
        yValues[n - 1] = yValues[n - 2];

        return factory.create(xValues, yValues);
    }

    public TabulatedFunction deriveSynchronously(TabulatedFunction function) {
        // Если функция уже синхронизированная — используем как есть
        SynchronizedTabulatedFunction syncFunc;
        if (function instanceof SynchronizedTabulatedFunction) {
            syncFunc = (SynchronizedTabulatedFunction) function;
        } else {
            syncFunc = new SynchronizedTabulatedFunction(function);
        }

        // Выполняем derive() внутри единого блока синхронизации
        return syncFunc.doSynchronously(this::derive);
    }
}