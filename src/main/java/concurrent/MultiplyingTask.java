package concurrent;
import functions.*;

public class MultiplyingTask implements Runnable {
    private final TabulatedFunction function;

    public MultiplyingTask(TabulatedFunction function) {
        this.function = function;
    }

    @Override
    public void run() {
        for (int i = 0; i < function.getCount(); i++) {
            synchronized (function) {
                double newY = function.getY(i) * 2;
                function.setY(i, newY);
            }
        }
        System.out.println("Поток " + Thread.currentThread().getName() + " закончил выполнение задачи.");
    }
}
