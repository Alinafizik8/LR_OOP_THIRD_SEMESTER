package concurrent;

import functions.ConstantFunction;
import functions.LinkedListTabulatedFunction;
import functions.TabulatedFunction;

public class ReadWriteTaskExecutor {
    public static void main(String[] args) {
        // Создаём табулированную функцию на основе ConstantFunction(-1)
        TabulatedFunction function = new LinkedListTabulatedFunction(
                new ConstantFunction(-1.0),
                1.0,      // xFrom
                1000.0,   // xTo
                1000      // count
        );

        // Создаём задачи
        ReadTask readTask = new ReadTask(function);
        WriteTask writeTask = new WriteTask(function, 0.5);

        // Создаём и запускаем потоки
        Thread reader = new Thread(readTask, "Reader");
        Thread writer = new Thread(writeTask, "Writer");

        reader.start();
        writer.start();

        // Ждём завершения обоих потоков
        try {
            reader.join();
            writer.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}