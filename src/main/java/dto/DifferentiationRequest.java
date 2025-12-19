package dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DifferentiationRequest {

    @NotNull(message = "Function ID is required")
    @JsonProperty("functionId")
    private Long functionId;

    @JsonProperty("functionType")
    private String functionType;
}
