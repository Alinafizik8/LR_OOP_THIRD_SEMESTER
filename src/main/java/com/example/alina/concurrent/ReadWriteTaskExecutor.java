package com.example.alina.concurrent;

import com.example.alina.functions.ConstantFunction;
import com.example.alina.functions.LinkedListTabulatedFunction;
import com.example.alina.functions.TabulatedFunction;

public class ReadWriteTaskExecutor {
    public static void main(String[] args) {
        // Создаём табулированную функцию на основе ConstantFunction(-1)
        TabulatedFunction function = new LinkedListTabulatedFunction(new ConstantFunction(-1.0), 1.0, 1000.0, 1000);

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