package com.example.alina.dto.function;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

//обновление имени функции

@Data
public class UpdateFunctionRequest {

    @NotNull(message = "Имя функции обязательно")
    @NotBlank(message = "Имя функции не может быть пустым")
    private String name;
}
