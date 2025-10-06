package operations;

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

        Point[] points = TabulatedFunctionOperationService.asPoints(function);
        int n = points.length;

        if (n < 2) {
            throw new IllegalArgumentException("Function must have at least 2 points for differentiation");
        }

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

        // Последняя точка (левая производная)
        yValues[n - 1] = (points[n - 1].y - points[n - 2].y) / (points[n - 1].x - points[n - 2].x);

        // Внутренние точки (центральная производная)
        for (int i = 1; i < n - 1; i++) {
            double h1 = points[i].x - points[i - 1].x;
            double h2 = points[i + 1].x - points[i].x;
            // Простая центральная разность (при равномерной сетке h1 == h2)
            yValues[i] = (points[i + 1].y - points[i - 1].y) / (points[i + 1].x - points[i - 1].x);
        }

        return factory.create(xValues, yValues);
    }
}