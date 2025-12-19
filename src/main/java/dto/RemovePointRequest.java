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
public class RemovePointRequest {
    @NotNull(message = "Index is required")
    @Min(value = 0, message = "Index must be non-negative")
    @JsonProperty("index")
    private Integer index;
}
