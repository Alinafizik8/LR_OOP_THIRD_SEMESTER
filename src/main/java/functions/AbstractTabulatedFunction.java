package functions;

import exceptions.*;

public abstract class AbstractTabulatedFunction implements MathFunction,Removable {

    //Возвращает количество точек в таблице
    protected abstract int getCount();

    //Возвращает значение x по индексу.
    protected abstract double getX(int index);

    //Возвращает значение y по индексу.
    protected abstract double getY(int index);

    protected abstract int indexOfX(double x);

    protected abstract int floorIndexOfX(double x);

    protected abstract double extrapolateLeft(double x);
    protected abstract double extrapolateRight(double x);

    protected abstract double interpolate(double x, int floorIndex);

    /**
     * Вспомогательный метод для линейной интерполяции между двумя точками.
     * Реализация одинакова для всех наследников.
     */
    protected static double interpolate(double x, double leftX, double rightX, double leftY, double rightY) {
        // Линейная интерполяция: y = y1 + (x - x1) * (y2 - y1) / (x2 - x1)
        return leftY + (x - leftX) * (rightY - leftY) / (rightX - leftX);
    }

    /**
     * Реализация метода из интерфейса MathFunction.
     * Обеспечивает полное поведение табулированной функции:
     * - экстраполяцию за пределами области определения,
     * - точное значение, если x есть в таблице,
     * - интерполяцию, если x внутри области, но отсутствует в таблице.
     */
    @Override
    public double apply(double x) {
        int count = getCount();
        double firstX = getX(0);
        double lastX = getX(count - 1);

        if (x < firstX) {
            return extrapolateLeft(x);
        }
        if (x > lastX) {
            return extrapolateRight(x);
        }

        // x находится внутри [firstX, lastX]
        int exactIndex = indexOfX(x);
        if (exactIndex != -1) {
            return getY(exactIndex);
        }

        // x не найден точно — выполняем интерполяцию
        int floorIndex = floorIndexOfX(x);
        // floorIndex гарантированно в диапазоне [0, count - 2]
        return interpolate(x, floorIndex);
    }

    static void checkLengthIsTheSame(double[] xValues, double[] yValues){
        if (xValues.length != yValues.length){
            throw new DifferentLengthOfArraysException("The length of the arrays cannot be different!");
        }
    };

    static void checkSorted(double[] xValues) {
        double x_prev = xValues[0];
        for (int i = 1; i < xValues.length; i++){
            if (xValues[i] > x_prev){
                x_prev = xValues[i];
            }
            else {
                throw new ArrayIsNotSortedException("The array must be sorted in ascending order!");
            }
        }
    };

}