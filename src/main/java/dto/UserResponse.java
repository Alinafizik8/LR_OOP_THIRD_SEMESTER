package dto;

import lombok.Data;
import java.time.Instant;

@Data
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String role;
    private Instant createdAt;
}
