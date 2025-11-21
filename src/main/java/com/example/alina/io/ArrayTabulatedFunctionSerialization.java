package com.example.alina.io;

import com.example.alina.functions.ArrayTabulatedFunction;
import com.example.alina.functions.TabulatedFunction;
import com.example.alina.operations.RightSteppingDifferentialOperator;
import com.example.alina.operations.TabulatedDifferentialOperator;
import java.io.*;

public class ArrayTabulatedFunctionSerialization {

    public static void main(String[] args) {
        String filePath = "output/serialized array functions.bin";

        // Этап 1: Сериализация
        try (FileOutputStream fileOutputStream = new FileOutputStream(filePath);
             BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream)) {

            // Создаём исходную функцию f(x) = x^2
            double[] xValues = {0.0, 1.0, 2.0, 3.0, 4.0};
            double[] yValues = {0.0, 1.0, 4.0, 9.0, 16.0};
            TabulatedFunction originalFunction = new ArrayTabulatedFunction(xValues, yValues);

            // Создаём оператор дифференцирования с шагом
            TabulatedDifferentialOperator differentialOperator = new TabulatedDifferentialOperator();

            // Вычисляем первую и вторую производные
            TabulatedFunction firstDerivative = differentialOperator.derive(originalFunction);
            TabulatedFunction secondDerivative = differentialOperator.derive(firstDerivative);

            // Сериализуем все три функции
            FunctionsIO.serialize(bufferedOutputStream, originalFunction);
            FunctionsIO.serialize(bufferedOutputStream, firstDerivative);
            FunctionsIO.serialize(bufferedOutputStream, secondDerivative);

        } catch (IOException e) {
            e.printStackTrace();
        }

        // Этап 2: Десериализация
        try (FileInputStream fileInputStream = new FileInputStream(filePath);
             BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream)) {

            // Десериализуем все три функции в том же порядке
            TabulatedFunction deserializedOriginal = FunctionsIO.deserialize(bufferedInputStream);
            TabulatedFunction deserializedFirst = FunctionsIO.deserialize(bufferedInputStream);
            TabulatedFunction deserializedSecond = FunctionsIO.deserialize(bufferedInputStream);

            // Выводим в консоль
            System.out.println("Original function:");
            System.out.println(deserializedOriginal);
            System.out.println("\nFirst derivative:");
            System.out.println(deserializedFirst);
            System.out.println("\nSecond derivative:");
            System.out.println(deserializedSecond);

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}