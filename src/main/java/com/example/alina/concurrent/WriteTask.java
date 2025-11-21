package com.example.alina.concurrent;

import com.example.alina.functions.TabulatedFunction;

public class WriteTask implements Runnable {
    private final TabulatedFunction function;
    private final double value;

    public WriteTask(TabulatedFunction function, double value) {
        this.function = function;
        this.value = value;
    }

    @Override
    public void run() {
        for (int i = 0; i < function.getCount(); ++i) {
            synchronized (function) {
                function.setY(i, value);
                System.out.printf("Writing for index %d complete%n", i);
            }
        }
    }
}