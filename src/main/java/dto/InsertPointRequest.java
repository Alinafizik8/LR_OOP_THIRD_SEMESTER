package dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InsertPointRequest {
    @NotNull(message = "X value is required")
    @JsonProperty("x")
    private Double x;

    @NotNull(message = "Y value is required")
    @JsonProperty("y")
    private Double y;
}
