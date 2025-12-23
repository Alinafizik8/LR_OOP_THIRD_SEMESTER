package operations;

import concurrent.SynchronizedTabulatedFunction;
import functions.*;
import functions.factory.TabulatedFunctionFactory;
import functions.factory.ArrayTabulatedFunctionFactory;

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

        int n = function.getCount();
        if (n < 2) {
            throw new IllegalArgumentException("Function must have at least 2 points");
        }

        double[] xValues = new double[n];
        double[] yValues = new double[n];

        // Копируем x
        for (int i = 0; i < n; i++) {
            xValues[i] = function.getX(i);
        }

        // Производные: используем среднюю производную в узле
        // f'(x_i) ≈ (f'_left + f'_right) / 2
        for (int i = 0; i < n; i++) {
            double leftDerivative = Double.NaN;
            double rightDerivative = Double.NaN;

            // Производная слева (если есть слева)
            if (i > 0) {
                double hLeft = xValues[i] - xValues[i - 1];
                leftDerivative = (function.getY(i) - function.getY(i - 1)) / hLeft;
            }

            // Производная справа (если есть справа)
            if (i < n - 1) {
                double hRight = xValues[i + 1] - xValues[i];
                rightDerivative = (function.getY(i + 1) - function.getY(i)) / hRight;
            }

            // Среднее (внутренние точки), или крайние значения
            if (i == 0) {
                yValues[i] = rightDerivative;  // первая точка — правая производная
            } else if (i == n - 1) {
                yValues[i] = leftDerivative;   // последняя — левая
            } else {
                yValues[i] = (leftDerivative + rightDerivative) / 2.0;
            }
        }

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