package com.example.alina.dto.function;

import lombok.Data;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

// создание из массивов x, y

@Data
public class CreateFunctionFromPointsRequest {

    @NotNull(message = "Имя функции обязательно")
    @NotEmpty(message = "Имя функции не может быть пустым")
    private String name;

    @NotNull(message = "Массив X значений обязателен")
    @Size(min = 2, message = "Функция должна содержать минимум 2 точки")
    private List<Double> xValues;

    @NotNull(message = "Массив Y значений обязателен")
    @Size(min = 2, message = "Функция должна содержать минимум 2 точки")
    private List<Double> yValues;

    // для тестирования
    private Long ownerId;
}
