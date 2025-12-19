package dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadFunctionRequest {

    @NotBlank(message = "Function name is required")
    @JsonProperty("name")
    private String name;

    @NotBlank(message = "Base64 data is required")
    @JsonProperty("base64Data")
    private String base64Data;

    @NotNull(message = "Function type is required")
    @JsonProperty("functionType")
    private String functionType;
}
