package dto.function;

import lombok.Data;
import java.time.Instant;

@Data
public class FunctionListDto {
    private Long id;
    private String name;
    private String type;
    private String localizedTypeName;
    private Instant createdAt;
    private Instant updatedAt;
}
