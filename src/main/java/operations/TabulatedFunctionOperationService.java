package operations;
import functions.*;
import functions.factory.*;
import exceptions.*;

public class TabulatedFunctionOperationService {

    @FunctionalInterface
    private interface  BiOperation {
        double apply(double u, double v);
    }

    TabulatedFunctionFactory factory;

    public TabulatedFunctionOperationService() {
        this.factory = new ArrayTabulatedFunctionFactory();
    }

    public TabulatedFunctionOperationService(TabulatedFunctionFactory factory) {
        if (factory == null) {
            throw new IllegalArgumentException("Factory cannot be null");
        }
        this.factory = factory;
    }

    TabulatedFunction doOperation(TabulatedFunction a, TabulatedFunction b, BiOperation operation){
        int countA = a.getCount();
        int countB = b.getCount();

        if (countA != countB) {
            throw new InconsistentFunctionsException("The number of points must match");
        }

        Point[] pointsA = asPoints(a);
        Point[] pointsB = asPoints(b);

        double[] xValues = new double[countA];
        double[] yValues = new double[countA];

        for (int i = 0; i < countA; i++) {
            double xA = pointsA[i].x;
            double xB = pointsB[i].x;

            // Сравнение с допуском для double
            if (Math.abs(xA - xB) > 1e-10) {
                throw new InconsistentFunctionsException("The values in the points must match");
            }

            xValues[i] = xA;
            yValues[i] = operation.apply(pointsA[i].y, pointsB[i].y);
        }

        return factory.create(xValues, yValues);
    }


    public TabulatedFunctionFactory getFactory() {
        return factory;
    }

    public void setFactory(TabulatedFunctionFactory factory) {
        if (factory == null) {
            throw new IllegalArgumentException("Factory cannot be null");
        }
        this.factory = factory;
    }

    public static Point[] asPoints(TabulatedFunction tabulatedFunction) {
        if (tabulatedFunction == null) {
            throw new NullPointerException("TabulatedFunction cannot be null");
        }
        // Получаем количество точек
        int count = tabulatedFunction.getCount();
        Point[] points = new Point[count];
        int i = 0;
        for (Point point : tabulatedFunction) {
            points[i] = point;
            i++;
        }
        return points;
    }

    public TabulatedFunction plus(TabulatedFunction a, TabulatedFunction b) {
        return doOperation(a, b, Double::sum);
    }

    public TabulatedFunction minus(TabulatedFunction a, TabulatedFunction b) {
        return doOperation(a, b, (u, v) -> u - v);
    }

    // Умножение: f(x) * g(x)
    public TabulatedFunction multiply(TabulatedFunction a, TabulatedFunction b) {
        return doOperation(a, b, (u, v) -> u * v);
    }

    // Деление: f(x) / g(x)
    public TabulatedFunction divide(TabulatedFunction a, TabulatedFunction b) {
        return doOperation(a, b, (u, v) -> {
            if (Math.abs(v) < 1e-12) {
                throw new ArithmeticException("Division by zero in tabulated function");
            }
            return u / v;
        });
    }
}
