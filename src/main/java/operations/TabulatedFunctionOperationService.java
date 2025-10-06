package operations;
import functions.*;

import java.util.ArrayList;
import java.util.List;

public class TabulatedFunctionOperationService {

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

}
