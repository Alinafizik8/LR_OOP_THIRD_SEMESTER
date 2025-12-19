package dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateFunctionFromArraysRequest {
    @NotBlank(message = "Function name is required")
    @JsonProperty("name")
    private String name;

    @NotNull(message = "X values are required")
    @Size(min = 2, message = "At least 2 points are required")
    @JsonProperty("xValues")
    private double[] xValues;

    @NotNull(message = "Y values are required")
    @Size(min = 2, message = "At least 2 points are required")
    @JsonProperty("yValues")
    private double[] yValues;

    @NotBlank(message = "Function type is required")
    @JsonProperty("functionType")
    private String functionType; // "ARRAY" or "LINKED_LIST"
}
