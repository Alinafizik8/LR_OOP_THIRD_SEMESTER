package com.example.alina.io;

import functions.*;
import java.io.*;

public class TabulatedFunctionFileWriter {

    public static void main(String[] args) {

        double[] xValues = {0.0, 1.0, 2.0, 3.0, 4.0};
        double[] yValues = {0.0, 1.0, 4.0, 9.0, 16.0}; // f(x) = x^2

        TabulatedFunction arrayFunction = new ArrayTabulatedFunction(xValues, yValues);
        TabulatedFunction linkedListFunction = new LinkedListTabulatedFunction(xValues, yValues);

        try (FileWriter fileWriter1 = new FileWriter("output/array_function.txt");
             FileWriter fileWriter2 = new FileWriter("output/linked_list_function.txt");
             BufferedWriter bufferedWriter1 = new BufferedWriter(fileWriter1);
             BufferedWriter bufferedWriter2 = new BufferedWriter(fileWriter2)) {

            // Записываем функции в соответствующие файлы
            FunctionsIO.writeTabulatedFunction(bufferedWriter1, arrayFunction);
            FunctionsIO.writeTabulatedFunction(bufferedWriter2, linkedListFunction);

        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

}
