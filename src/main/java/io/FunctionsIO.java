package io;
import functions.*;
import functions.factory.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

public final class FunctionsIO {
    private static final Logger logger = LoggerFactory.getLogger(FunctionsIO.class);

    private FunctionsIO() {
        throw new UnsupportedOperationException("Utility class FunctionsIO cannot be instantiated");
    }

    public static void serialize(BufferedOutputStream stream, TabulatedFunction function) throws IOException {
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(stream);
        objectOutputStream.writeObject(function);
        objectOutputStream.flush();
    }

    public static void writeTabulatedFunction(BufferedWriter writer, TabulatedFunction function) throws IOException {
        logger.info("Starting text serialization of function {} with {} points to writer", function.getClass().getSimpleName(), function.getCount());

        PrintWriter printWriter = new PrintWriter(writer);
        int count = function.getCount();
        printWriter.println(count);

        for (var point : function) {
            printWriter.printf("%f %f%n", point.x, point.y);
        }
        printWriter.flush();

        if (printWriter.checkError()) {
            logger.error("IO error detected by PrintWriter during write operation for function {}", function.getClass().getSimpleName());
            throw new IOException("IO error occurred during writing");
        }

    }

    public static TabulatedFunction readTabulatedFunction(BufferedReader reader, TabulatedFunctionFactory factory) throws IOException {
        logger.info("Starting text deserialization of function from reader using factory {}", factory.getClass().getSimpleName());
        // Читаем первую строку — количество точек
        String countLine = reader.readLine();
        if (countLine == null) {
            logger.error("Unexpected end of stream: missing count line");
            throw new IOException("Unexpected end of stream: missing count line");
        }
        int count;
        try {
            count = Integer.parseInt(countLine.trim());
        }
        catch (NumberFormatException e) {
            logger.error("Invalid count format: ", e);
            throw new IOException("Invalid count format: " + countLine, e);
        }

        if (count <= 0) {
            logger.error("Count must be positive, got: " + count);
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
        TabulatedFunction result = factory.create(xValues, yValues);
        logger.info("Function {} successfully deserialized from text format using factory {}", result.getClass().getSimpleName(), factory.getClass().getSimpleName());
        return result;
    }

    /**
     * Десериализует табулированную функцию из буферизованного байтового потока.
     *
     * @param stream буферизованный входной поток
     * @return десериализованная функция
     * @throws IOException если произошла ошибка ввода-вывода
     * @throws ClassNotFoundException если класс функции не найден
     */
    public static TabulatedFunction deserialize(BufferedInputStream stream)
            throws IOException, ClassNotFoundException {
        ObjectInputStream objectIn = new ObjectInputStream(stream);
        return (TabulatedFunction) objectIn.readObject();
    }

    /**
     * Записывает табулированную функцию в байтовый поток.
     * Формат: count (int), затем пары (x, y) как double.
     */
    public static void writeTabulatedFunction(BufferedOutputStream outputStream, TabulatedFunction function)
            throws IOException {
        logger.info("Starting binary serialization of function {} with {} points to stream", function.getClass().getSimpleName(), function.getCount());
        DataOutputStream dataOut = new DataOutputStream(outputStream);
        int count = function.getCount();
        dataOut.writeInt(count);
        for (functions.Point point : function) {
            dataOut.writeDouble(point.x);
            dataOut.writeDouble(point.y);
        }
        dataOut.flush();
        logger.info("Function {} successfully serialized in binary format and written to stream", function.getClass().getSimpleName());
    }

    public static TabulatedFunction readTabulatedFunction(
            BufferedInputStream inputStream,
            TabulatedFunctionFactory factory
    ) throws IOException {
        logger.info("Starting binary deserialization of function from stream using factory {}", factory.getClass().getSimpleName());
        DataInputStream dataIn = new DataInputStream(inputStream);
        int count = dataIn.readInt();
        double[] xValues = new double[count];
        double[] yValues = new double[count];
        for (int i = 0; i < count; i++) {
            xValues[i] = dataIn.readDouble();
            yValues[i] = dataIn.readDouble();
        }
        TabulatedFunction result = factory.create(xValues, yValues);
        logger.info("Function {} successfully deserialized from binary format using factory {}", result.getClass().getSimpleName(), factory.getClass().getSimpleName());
        return result;
    }
}
