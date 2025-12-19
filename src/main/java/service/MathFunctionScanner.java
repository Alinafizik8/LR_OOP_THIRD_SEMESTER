package service;

import functions.MathFunction;
import functions.MathFunctionInfo;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MathFunctionScanner {

    private final Map<String, MathFunctionMetadata> functionMetadata;

    public static class MathFunctionMetadata {
        private final String className;
        private final String localizedName;
        private final int priority;

        public MathFunctionMetadata(String className, String localizedName, int priority) {
            this.className = className;
            this.localizedName = localizedName;
            this.priority = priority;
        }

        public String getClassName() {
            return className;
        }

        public String getLocalizedName() {
            return localizedName;
        }

        public int getPriority() {
            return priority;
        }
    }

    public MathFunctionScanner() {
        this.functionMetadata = scanMathFunctions();
    }

    /**
     * Сканирует пакет functions и находит все классы, помеченные аннотацией @MathFunctionInfo
     */
    private Map<String, MathFunctionMetadata> scanMathFunctions() {
        Map<String, MathFunctionMetadata> metadata = new HashMap<>();

        try {
            Reflections reflections = new Reflections("functions", Scanners.TypesAnnotated);
            Set<Class<?>> annotatedClasses = reflections.getTypesAnnotatedWith(MathFunctionInfo.class);

            for (Class<?> clazz : annotatedClasses) {
                // Проверяем, что класс реализует MathFunction
                if (MathFunction.class.isAssignableFrom(clazz)) {
                    MathFunctionInfo annotation = clazz.getAnnotation(MathFunctionInfo.class);
                    String className = clazz.getSimpleName();

                    metadata.put(className, new MathFunctionMetadata(
                            className,
                            annotation.name(),
                            annotation.priority()
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return metadata;
    }

    /**
     * Возвращает список всех зарегистрированных функций, отсортированный по приоритету
     */
    public List<MathFunctionMetadata> getAllFunctions() {
        return functionMetadata.values().stream()
                .sorted(Comparator.comparingInt(MathFunctionMetadata::getPriority))
                .collect(Collectors.toList());
    }

    /**
     * Возвращает метаданные функции по её имени класса
     */
    public MathFunctionMetadata getFunctionMetadata(String className) {
        return functionMetadata.get(className);
    }

    /**
     * Возвращает локализованное имя функции по имени класса
     */
    public String getLocalizedName(String className) {
        MathFunctionMetadata meta = functionMetadata.get(className);
        return meta != null ? meta.getLocalizedName() : className;
    }
}
