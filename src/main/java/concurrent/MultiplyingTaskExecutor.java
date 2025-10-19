package concurrent;
import functions.*;
import java.util.ArrayList;
import java.util.List;

public class MultiplyingTaskExecutor {

    public static void main(String[] args) throws InterruptedException {

        TabulatedFunction function = new LinkedListTabulatedFunction(new UnitFunction(), 1.0, 1000.0, 1000);

        List<Thread> threads = new ArrayList<>();
        int threadCount = 10;

        for (int i = 0; i < threadCount; i++) {
            MultiplyingTask task = new MultiplyingTask(function);
            Thread thread = new Thread(task);
            threads.add(thread);
        }

        for (Thread thread : threads) {
            thread.start();
        }

        Thread.sleep(2000);

        double finalY = function.getY(0);
        System.out.println("Итоговое y[0] = " + finalY);
        System.out.println("Ожидаемое значение: " + Math.pow(2, threadCount));
    }

}
