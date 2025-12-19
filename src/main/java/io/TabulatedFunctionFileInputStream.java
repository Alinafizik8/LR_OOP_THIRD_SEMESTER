package io;

import functions.TabulatedFunction;
import functions.factory.ArrayTabulatedFunctionFactory;
import functions.factory.LinkedListTabulatedFunctionFactory;
import operations.TabulatedDifferentialOperator;

import java.io.*;

public class TabulatedFunctionFileInputStream {
    public static void main(String[] args) {
        // Часть 1: Чтение из файла
        try (
                FileInputStream fileIn = new FileInputStream("input/binary function.bin");
                BufferedInputStream bufIn = new BufferedInputStream(fileIn)
        ) {
            TabulatedFunction func = FunctionsIO.readTabulatedFunction(
                    bufIn,
                    new ArrayTabulatedFunctionFactory()
            );
            System.out.println(func);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Часть 2: Чтение из консоли
        System.out.println("Введите размер и значения функции");
        try {
            InputStreamReader isr = new InputStreamReader(System.in);
            BufferedReader reader = new BufferedReader(isr);

            TabulatedFunction consoleFunc = FunctionsIO.readTabulatedFunction(
                    reader,
                    new LinkedListTabulatedFunctionFactory()
            );

            TabulatedDifferentialOperator diffOp = new TabulatedDifferentialOperator();
            TabulatedFunction derivative = diffOp.derive(consoleFunc);
            System.out.println(derivative);

            // НЕ закрываем reader — не закрываем System.in
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}