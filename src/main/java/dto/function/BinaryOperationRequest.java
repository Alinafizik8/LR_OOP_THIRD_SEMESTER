package dto.function;

import lombok.Data;
import jakarta.validation.constraints.NotNull;

@Data
public class BinaryOperationRequest {

    @NotNull(message = "ID первой функции обязателен")
    private Long functionId1;

    @NotNull(message = "ID второй функции обязателен")
    private Long functionId2;

    @NotNull(message = "Имя результирующей функции обязательно")
    private String resultName;

    private Long ownerId;
}
