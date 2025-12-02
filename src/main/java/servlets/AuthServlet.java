package servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.User;
import service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.spec.SecretKeySpec;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@WebServlet("/api/v1/auth/*")
public class AuthServlet extends HttpServlet {
    private static final String SECRET_KEY = "your_very_secret_key_here_change_it_in_production"; // ⚠️ Замените!
    private static final long EXPIRATION_TIME = 864_000_000; // 10 дней

    private final UserService userService = new UserService();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {
            if ("/login".equals(path)) {
                handleLogin(req, resp);
            } else if ("/register".equals(path)) {
                handleRegister(req, resp);
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Внутренняя ошибка сервера");
            mapper.writeValue(resp.getWriter(), error);
        }
    }

    private void handleLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Map<String, String> input = mapper.readValue(req.getInputStream(), Map.class);
        String username = input.get("username");
        String password = input.get("password");

        if (username == null || password == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Логин и пароль обязательны");
            mapper.writeValue(resp.getWriter(), error);
            return;
        }

        User user = userService.authenticate(username, password);
        if (user == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Неверный логин или пароль");
            mapper.writeValue(resp.getWriter(), error);
            return;
        }

        // Генерируем JWT
        Key key = getSigningKey();
        String token = Jwts.builder()
                .setSubject(user.getUsername())
                .claim("id", user.getId())
                .claim("role", user.getRole())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "role", user.getRole()
        ));

        resp.setStatus(HttpServletResponse.SC_OK);
        mapper.writeValue(resp.getWriter(), result);
    }

    private void handleRegister(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Map<String, String> input = mapper.readValue(req.getInputStream(), Map.class);
        String username = input.get("username");
        String password = input.get("password");

        if (username == null || username.length() < 3) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Логин должен быть не короче 3 символов");
            mapper.writeValue(resp.getWriter(), error);
            return;
        }
        if (password == null || password.length() < 4) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Пароль должен быть не короче 4 символов");
            mapper.writeValue(resp.getWriter(), error);
            return;
        }

        try {
            userService.register(username, password);
            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.getWriter().write("{\"message\":\"Регистрация успешна\"}");
        } catch (RuntimeException e) { // например, "пользователь уже существует"
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            mapper.writeValue(resp.getWriter(), error);
        }
    }

    private Key getSigningKey() {
        byte[] keyBytes = SECRET_KEY.getBytes();
        return new SecretKeySpec(keyBytes, SignatureAlgorithm.HS256.getJcaName());
    }
}