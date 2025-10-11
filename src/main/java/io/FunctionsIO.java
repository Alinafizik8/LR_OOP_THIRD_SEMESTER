package io;
import functions.*;
import java.io.BufferedWriter;
import java.io.PrintWriter;

public final class FunctionsIO {

    private FunctionsIO() {
        throw new UnsupportedOperationException("Utility class FunctionsIO cannot be instantiated");
    }

    public static void writeTabulatedFunction(BufferedWriter writer, TabulatedFunction function) throws java.io.IOException {

        PrintWriter printWriter = new PrintWriter(writer);
        int count = function.getCount();
        printWriter.println(count);

        for (var point : function) {
            printWriter.printf("%f %f%n", point.x, point.y);
        }
        printWriter.flush();

    }

}
