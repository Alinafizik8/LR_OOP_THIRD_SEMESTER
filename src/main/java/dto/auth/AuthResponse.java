package dto.auth;

import dto.user.UserDto;
import lombok.Data;
import java.time.Instant;

@Data
public class AuthResponse {

    private String token;
    private long expiresIn;
    private UserDto user;
}