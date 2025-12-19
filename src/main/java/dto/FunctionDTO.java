package dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FunctionDTO {
    private Long id;
    private String name;
    private String functionType;

    @JsonProperty("xValues")
    private double[] xValues;

    @JsonProperty("yValues")
    private double[] yValues;

    private Integer count;
}
