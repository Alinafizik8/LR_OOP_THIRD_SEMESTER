package concurrent;

import functions.TabulatedFunction;
import functions.Point;
import functions.factory.TabulatedFunctionFactory;
import operations.TabulatedFunctionOperationService;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class SynchronizedTabulatedFunction implements TabulatedFunction {
    private final TabulatedFunction function;

    public SynchronizedTabulatedFunction(TabulatedFunction function) {
        if (function == null) {
            throw new IllegalArgumentException("Function must not be null");
        }
        this.function = function;
    }

    // Вспомогательный метод для синхронизации
    private <T> T syncCall(java.util.function.Supplier<T> action) {
        synchronized (function) {
            return action.get();
        }
    }

    private void syncRun(Runnable action) {
        synchronized (function) {
            action.run();
        }
    }

    // <<<<>>>> Делегирование всех методов с синхронизацией

    @Override
    public int getCount() {
        return syncCall(function::getCount);
    }

    @Override
    public double getX(int index) {
        return syncCall(() -> function.getX(index));
    }

    @Override
    public double getY(int index) {
        return syncCall(() -> function.getY(index));
    }

    @Override
    public void setY(int index, double value) {
        syncRun(() -> function.setY(index, value));
    }

    @Override
    public int indexOfX(double x) {
        return syncCall(() -> function.indexOfX(x));
    }

    @Override
    public int indexOfY(double y) {
        return syncCall(() -> function.indexOfY(y));
    }

    @Override
    public double leftBound() {
        return syncCall(function::leftBound);
    }

    @Override
    public double rightBound() {
        return syncCall(function::rightBound);
    }

    @Override
    public double apply(double x) {
        return syncCall(() -> function.apply(x));
    }

    // <<<<>>>> Переписанный iterator()
    @Override
    public Iterator<Point> iterator() {
        Point[] points;
        synchronized (function) {
            points = TabulatedFunctionOperationService.asPoints(function);
        }

        return new Iterator<Point>() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < points.length;
            }

            @Override
            public Point next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return points[index++];
            }
        };
    }
}