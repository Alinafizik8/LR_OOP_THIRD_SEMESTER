package dto.function;

import lombok.Data;
import java.util.List;

//полная функция с точками

@Data
public class FullFunctionResponse extends FunctionMetadataResponse {

    private List<PointDto> points; // Список точек (x, y)

    // класс для одной точки
    @Data
    public static class PointDto {
        private Double x;
        private Double y;
    }
}
