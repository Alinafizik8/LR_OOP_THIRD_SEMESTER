package io;
import functions.*;
import functions.factory.*;

import java.io.*;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

public final class FunctionsIO {

    private FunctionsIO() {
        throw new UnsupportedOperationException("Utility class FunctionsIO cannot be instantiated");
    }

    public static void serialize(BufferedOutputStream stream, TabulatedFunction function) throws IOException {
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(stream);
        objectOutputStream.writeObject(function);
        objectOutputStream.flush();
    }

    public static void writeTabulatedFunction(BufferedWriter writer, TabulatedFunction function) throws IOException {

        PrintWriter printWriter = new PrintWriter(writer);
        int count = function.getCount();
        printWriter.println(count);

        for (var point : function) {
            printWriter.printf("%f %f%n", point.x, point.y);
        }
        printWriter.flush();

        if (printWriter.checkError()) {
            throw new IOException("IO error occurred during writing");
        }

    }

    public static TabulatedFunction readTabulatedFunction(BufferedReader reader, TabulatedFunctionFactory factory) throws IOException {
        // Читаем первую строку — количество точек
        String countLine = reader.readLine();
        if (countLine == null) {
            throw new IOException("Unexpected end of stream: missing count line");
        }
        int count;
        try {
            count = Integer.parseInt(countLine.trim());
        }
        catch (NumberFormatException e) {
            throw new IOException("Invalid count format: " + countLine, e);
        }

        if (count <= 0) {
            throw new IOException("Count must be positive, got: " + count);
        }

        // Создаём массивы для координат
        double[] xValues = new double[count];
        double[] yValues = new double[count];

        // Получаем форматтер для чисел с запятой (русская локаль)
        NumberFormat numberFormat = NumberFormat.getInstance(Locale.forLanguageTag("ru"));

        // Читаем count строк с парами x y
        for (int i = 0; i < count; i++) {
            String line = reader.readLine();
            if (line == null) {
                throw new IOException("Unexpected end of stream at line " + (i + 2));
            }

            // Разбиваем строку по пробелу
            String[] parts = line.trim().split(" ");
            if (parts.length != 2) {
                throw new IOException("Invalid line format at line " + (i + 2) + ": " + line);
            }

            try {
                // Парсим числа с запятой как десятичным разделителем
                xValues[i] = numberFormat.parse(parts[0]).doubleValue();
                yValues[i] = numberFormat.parse(parts[1]).doubleValue();
            } catch (ParseException e) {
                // Оборачиваем ParseException в IOException, как требуется
                throw new IOException("Failed to parse number at line " + (i + 2) + ": " + line, e);
            }
        }

        // Создаём и возвращаем функцию через фабрику
        return factory.create(xValues, yValues);
    }

    public static TabulatedFunction deserialize(BufferedInputStream stream)
            throws IOException, ClassNotFoundException {
        ObjectInputStream objectInputStream = new ObjectInputStream(stream);
        return (TabulatedFunction) objectInputStream.readObject();
    }

}
