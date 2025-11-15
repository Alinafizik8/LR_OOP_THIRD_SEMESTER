package dto;

import lombok.Data;
import java.util.List;

@Data
public class PagedResponse<T> {

    private List<T> content;
    private PageableInfo pageable;

    @Data
    public static class PageableInfo {
        private int pageNumber;
        private int pageSize;
        private int totalPages;
        private long totalElements;
    }
}
