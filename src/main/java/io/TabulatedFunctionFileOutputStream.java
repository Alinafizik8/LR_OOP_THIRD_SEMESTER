package io;

import functions.ArrayTabulatedFunction;
import functions.LinkedListTabulatedFunction;
import functions.TabulatedFunction;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class TabulatedFunctionFileOutputStream {
    public static void main(String[] args) {
        // Создаём табулированные функции
        double[] x = {0.0, 1.0, 2.0, 3.0};
        double[] y = {0.0, 1.0, 4.0, 9.0}; // f(x) = x^2

        TabulatedFunction arrayFunc = new ArrayTabulatedFunction(x, y);
        TabulatedFunction linkedFunc = new LinkedListTabulatedFunction(x, y);

        try (
                BufferedOutputStream bufOut1 = new BufferedOutputStream(new FileOutputStream("output/array function.bin"));
                BufferedOutputStream bufOut2 = new BufferedOutputStream(new FileOutputStream("output/linked list function.bin"))
        ) {
            FunctionsIO.writeTabulatedFunction(bufOut1, arrayFunc);
            FunctionsIO.writeTabulatedFunction(bufOut2, linkedFunc);
            System.out.println("Файлы успешно записаны в папку output/");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}