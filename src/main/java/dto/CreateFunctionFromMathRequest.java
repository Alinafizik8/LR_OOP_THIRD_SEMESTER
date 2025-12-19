package dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateFunctionFromMathRequest {
    @NotBlank(message = "Function name is required")
    @JsonProperty("name")
    private String name;

    @NotBlank(message = "Math function type is required")
    @JsonProperty("mathFunctionType")
    private String mathFunctionType; // "SQR", "IDENTITY", "UNIT", "ZERO", "CONSTANT"

    @JsonProperty("constantValue")
    private Double constantValue; // For CONSTANT function

    @NotNull(message = "From value is required")
    @JsonProperty("xFrom")
    private Double xFrom;

    @NotNull(message = "To value is required")
    @JsonProperty("xTo")
    private Double xTo;

    @NotNull(message = "Count is required")
    @Min(value = 2, message = "At least 2 points are required")
    @JsonProperty("count")
    private Integer count;

    @NotBlank(message = "Function type is required")
    @JsonProperty("functionType")
    private String functionType; // "ARRAY" or "LINKED_LIST"
}
