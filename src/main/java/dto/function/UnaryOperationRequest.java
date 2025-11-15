package dto.function;

import lombok.Data;
import jakarta.validation.constraints.NotNull;

// дифференцирование

@Data
public class UnaryOperationRequest {

    @NotNull(message = "ID функции обязателен")
    private Long functionId;

    @NotNull(message = "Имя результирующей функции обязательно")
    private String resultName;

    private Long ownerId;
}
