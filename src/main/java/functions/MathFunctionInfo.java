package functions;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для маркировки математических функций.
 * Позволяет автоматически сканировать и регистрировать функции с их локализованными именами.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface MathFunctionInfo {
    /**
     * Локализованное имя функции на русском языке
     */
    String name();

    /**
     * Приоритет функции для сортировки в списке (меньшее значение = выше в списке)
     */
    int priority() default 100;
}
