package service;

import dto.user.UserDto;
import entity.UserEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserService userService;
    public CustomUserDetailsService(UserService userService) { this.userService = userService; }

    @Override
    public UserDetails loadUserByUsername(String username) {
        UserEntity u = userService.findUserEntityByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));
        return User.builder()
                .username(u.getUsername())
                .password(u.getPasswordHash())
                .authorities("ROLE_" + u.getRole().toUpperCase())
                .build();
    }
}
