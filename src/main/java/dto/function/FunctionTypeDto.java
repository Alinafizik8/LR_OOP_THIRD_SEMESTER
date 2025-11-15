package dto.function;

import lombok.Data;
import java.time.Instant;

@Data
public class FunctionTypeDto {

    private Long id;
    private String name;
    private String localizedName;
    private Integer priority;
    private Instant createdAt;
    private Instant updatedAt;
}
