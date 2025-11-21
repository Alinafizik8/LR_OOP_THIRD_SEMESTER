package com.example.alina.io;

import com.example.alina.functions.LinkedListTabulatedFunction;
import com.example.alina.functions.TabulatedFunction;
import com.example.alina.operations.TabulatedDifferentialOperator;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class LinkedListTabulatedFunctionSerialization {
    public static void main(String[] args) {
        String filePath = "output/serialized linked list functions.bin";

        // Часть 1: Сериализация
        try (
                FileOutputStream fileOut = new FileOutputStream(filePath);
                BufferedOutputStream bufOut = new BufferedOutputStream(fileOut)
        ) {
            // Создаём исходную функцию: f(x) = x^2 на [0, 2] с 5 точками
            double[] x = {0.0, 0.5, 1.0, 1.5, 2.0};
            double[] y = {0.0, 0.25, 1.0, 2.25, 4.0};
            TabulatedFunction original = new LinkedListTabulatedFunction(x, y);

            TabulatedDifferentialOperator diffOp = new TabulatedDifferentialOperator();

            // Первая производная: f'(x) = 2x
            TabulatedFunction firstDerivative = diffOp.derive(original);

            // Вторая производная: f''(x) = 2
            TabulatedFunction secondDerivative = diffOp.derive(firstDerivative);

            // Сериализуем все три функции
            FunctionsIO.serialize(bufOut, original);
            FunctionsIO.serialize(bufOut, firstDerivative);
            FunctionsIO.serialize(bufOut, secondDerivative);

            System.out.println("Сериализация завершена.");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Часть 2: Десериализация
        try (
                FileInputStream fileIn = new FileInputStream(filePath);
                BufferedInputStream bufIn = new BufferedInputStream(fileIn)
        ) {
            TabulatedFunction original = FunctionsIO.deserialize(bufIn);
            TabulatedFunction firstDerivative = FunctionsIO.deserialize(bufIn);
            TabulatedFunction secondDerivative = FunctionsIO.deserialize(bufIn);

            System.out.println("<<< Исходная функция >>>");
            System.out.println(original);
            System.out.println("\n<<< Первая производная >>>");
            System.out.println(firstDerivative);
            System.out.println("\n<<< Вторая производная >>>");
            System.out.println(secondDerivative);

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}