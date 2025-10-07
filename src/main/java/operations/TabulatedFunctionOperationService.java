package operations;
import functions.*;
import functions.factory.*;

public class TabulatedFunctionOperationService {

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
        // Получаем количество точек — предполагаем, что такой метод есть
        int count = tabulatedFunction.getCount(); // или size()
        Point[] points = new Point[count];
        int i = 0;
        for (Point point : tabulatedFunction) {
            points[i] = point;
            i++;
        }
        return points;
    }

    //методы сложения и методы вычитания должны быть тут

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
