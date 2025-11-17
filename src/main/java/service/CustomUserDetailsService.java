package service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService, Serializable {
    @Serial
    private static final long serialVersionUID = -2997767373270645069L;

    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

    private final UserService userService;
    public CustomUserDetailsService(UserService userService) { this.userService = userService; }

    @Override
    public UserDetails loadUserByUsername(String username) {
        return userService.findUserEntityByUsername(username)
                .map(entity -> {
                    logger.debug("Authenticated: '{}', role={}", entity.getUsername(), entity.getRole());
                    return new CustomUserDetails(
                            entity.getId(),
                            entity.getUsername(),
                            entity.getPasswordHash(),
                            List.of(new SimpleGrantedAuthority("ROLE_" + entity.getRole().toUpperCase()))
                    );
                })
                .orElseThrow(() -> {
                    logger.warn("Authentication failed for user: '{}'", username);
                    return new UsernameNotFoundException("Invalid credentials");
                });
    }
}
