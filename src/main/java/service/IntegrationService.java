package service;

import functions.TabulatedFunction;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Service
public class IntegrationService {

    private static final int MAX_THREADS = 16;

    /**
     * Вычисляет определенный интеграл табулированной функции методом трапеций с использованием многопоточности
     *
     * @param function табулированная функция
     * @param lowerLimit нижняя граница интегрирования
     * @param upperLimit верхняя граница интегрирования
     * @param threadCount количество потоков (ограничено MAX_THREADS)
     * @return значение интеграла
     */
    public IntegrationResult calculateIntegral(
            TabulatedFunction function,
            double lowerLimit,
            double upperLimit,
            int threadCount) throws InterruptedException, ExecutionException {

        if (lowerLimit >= upperLimit) {
            throw new IllegalArgumentException("Lower limit must be less than upper limit");
        }

        // Ограничиваем количество потоков
        threadCount = Math.min(threadCount, MAX_THREADS);
        threadCount = Math.max(threadCount, 1);

        long startTime = System.currentTimeMillis();

        // Разделяем интервал на части для каждого потока
        double intervalLength = (upperLimit - lowerLimit) / threadCount;

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<Future<Double>> futures = new ArrayList<>();

        // Создаем задачи для каждого потока
        for (int i = 0; i < threadCount; i++) {
            final double segmentStart = lowerLimit + i * intervalLength;
            final double segmentEnd = (i == threadCount - 1) ? upperLimit : segmentStart + intervalLength;

            Callable<Double> task = () -> calculateSegmentIntegral(function, segmentStart, segmentEnd);
            futures.add(executor.submit(task));
        }

        // Собираем результаты
        double totalIntegral = 0.0;
        for (Future<Double> future : futures) {
            totalIntegral += future.get();
        }

        executor.shutdown();
        long endTime = System.currentTimeMillis();

        return new IntegrationResult(
                totalIntegral,
                endTime - startTime,
                threadCount
        );
    }

    /**
     * Вычисляет интеграл на сегменте методом трапеций
     */
    private double calculateSegmentIntegral(TabulatedFunction function, double start, double end) {
        // Используем метод трапеций с адаптивным шагом
        int steps = 1000;
        double h = (end - start) / steps;
        double sum = 0.0;

        for (int i = 0; i < steps; i++) {
            double x1 = start + i * h;
            double x2 = start + (i + 1) * h;
            double y1 = function.apply(x1);
            double y2 = function.apply(x2);

            // Площадь трапеции: (y1 + y2) * h / 2
            sum += (y1 + y2) * h / 2.0;
        }

        return sum;
    }

    /**
     * Результат вычисления интеграла
     */
    public static class IntegrationResult {
        private final double value;
        private final long computationTimeMs;
        private final int threadsUsed;

        public IntegrationResult(double value, long computationTimeMs, int threadsUsed) {
            this.value = value;
            this.computationTimeMs = computationTimeMs;
            this.threadsUsed = threadsUsed;
        }

        public double getValue() {
            return value;
        }

        public long getComputationTimeMs() {
            return computationTimeMs;
        }

        public int getThreadsUsed() {
            return threadsUsed;
        }
    }
}
