package com.example.alina.dto.function;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

// создание из математической функции

@Data
public class CreateFunctionFromMathRequest {

    @NotNull(message = "Имя функции обязательно")
    @NotBlank(message = "Имя функции не может быть пустым")
    private String name;

    @NotNull(message = "Тип математической функции обязателен")
    @NotBlank(message = "Тип математической функции не может быть пустым")
    private String mathFunctionType; // например, "SqrFunction"

    @NotNull(message = "Начальная точка интервала обязательна")
    private Double xFrom;

    @NotNull(message = "Конечная точка интервала обязательна")
    private Double xTo;

    @NotNull(message = "Количество точек обязательно")
    @Positive(message = "Количество точек должно быть положительным")
    private Integer count;

    private Long ownerId;
}
