package dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IntegrationRequest {
    @NotNull(message = "Function ID is required")
    @JsonProperty("functionId")
    private Long functionId;

    @NotNull(message = "Lower limit is required")
    @JsonProperty("lowerLimit")
    private Double lowerLimit;

    @NotNull(message = "Upper limit is required")
    @JsonProperty("upperLimit")
    private Double upperLimit;

    @NotNull(message = "Thread count is required")
    @Min(value = 1, message = "Thread count must be at least 1")
    @JsonProperty("threadCount")
    private Integer threadCount;
}
