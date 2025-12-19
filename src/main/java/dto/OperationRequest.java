package dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OperationRequest {

    @NotNull(message = "First function ID is required")
    @JsonProperty("firstFunctionId")
    private Long firstFunctionId;

    @NotNull(message = "Second function ID is required")
    @JsonProperty("secondFunctionId")
    private Long secondFunctionId;

    @NotNull(message = "Operation type is required")
    @JsonProperty("operation")
    private String operation; // PLUS, MINUS, MULTIPLY, DIVIDE

    @JsonProperty("functionType")
    private String functionType;
}
