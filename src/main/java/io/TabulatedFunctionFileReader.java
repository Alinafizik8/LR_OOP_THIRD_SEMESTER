package io;

import functions.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class TabulatedFunctionFileReader {

    public static void main(String[] args) {
        try (FileReader fileReader1 = new FileReader("input/function.txt");
             FileReader fileReader2 = new FileReader("input/function.txt");
             BufferedReader bufferedReader1 = new BufferedReader(fileReader1);
             BufferedReader bufferedReader2 = new BufferedReader(fileReader2)) {

            // Чтение функций с использованием соответствующих фабрик
            TabulatedFunction arrayFunction = FunctionsIO.readTabulatedFunction(
                    bufferedReader1,
                    ArrayTabulatedFunction::new
            );

            TabulatedFunction linkedListFunction = FunctionsIO.readTabulatedFunction(
                    bufferedReader2,
                    LinkedListTabulatedFunction::new
            );

            // Вывод функций в консоль
            System.out.println(arrayFunction);
            System.out.println(linkedListFunction);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
