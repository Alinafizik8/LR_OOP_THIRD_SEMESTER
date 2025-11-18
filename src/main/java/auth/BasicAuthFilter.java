package auth;

import model.User;
import service.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Base64;

public class BasicAuthFilter implements Filter {
    private static final Logger log = LogManager.getLogger(BasicAuthFilter.class);
    private UserService userService;

    @Override
    public void init(FilterConfig filterConfig) {
        userService = new UserService();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String authHeader = httpRequest.getHeader("Authorization");

        User user = null;
        if (authHeader != null && authHeader.startsWith("Basic ")) {
            String base64Credentials = authHeader.substring(6);
            String credentials = new String(Base64.getDecoder().decode(base64Credentials));
            String[] parts = credentials.split(":", 2);
            if (parts.length == 2) {
                String username = parts[0];
                String password = parts[1];
                user = userService.authenticate(username, password);
                if (user != null) {
                    log.info("Authenticated user: {}", username);
                } else {
                    log.warn("Failed authentication for username: {}", username);
                }
            }
        }

        SecurityContext.setCurrentUser(user);

        if (user == null) {
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.setHeader("WWW-Authenticate", "Basic realm=\"Secure Area\"");
            return;
        }

        try {
            chain.doFilter(request, response);
        } finally {
            SecurityContext.clear();
        }
    }

    @Override
    public void destroy() {}
}