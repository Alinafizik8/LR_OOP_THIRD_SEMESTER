package servlets;

import dto.UserDTO;
import functions.dao.UserDao;
import functions.dao.UserDaoImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;

@WebServlet("/api/v1/users/*")
public class UserServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(UserServlet.class);
    private final UserDao userDao = new UserDaoImpl(getDataSource());
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        resp.setContentType("application/json; charset=UTF-8");

        if (pathInfo == null || pathInfo.equals("/")) {
            // GET /api/users - получить всех пользователей
            List<UserDTO> users = userDao.findAll();
            writeJsonResponse(resp, users);
        } else {
            // GET /api/users/{id} - получить пользователя по ID
            String idStr = pathInfo.substring(1); // убираем первый '/'
            try {
                Long id = Long.parseLong(idStr);
                Optional<UserDTO> userOpt = userDao.findById(id);
                if (userOpt.isPresent()) {
                    writeJsonResponse(resp, userOpt.get());
                } else {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    resp.getWriter().write("{\"error\": \"User not found\"}");
                }
            } catch (NumberFormatException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\": \"Invalid ID format\"}");
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json; charset=UTF-8");
        UserDTO userDto = readJsonRequest(req, UserDTO.class);

        if (userDto == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"Invalid request body\"}");
            return;
        }

        try {
            Long savedId = userDao.save(userDto);
            userDto.setId(savedId);
            resp.setStatus(HttpServletResponse.SC_CREATED);
            writeJsonResponse(resp, userDto);
        } catch (Exception e) {
            logger.error("Error saving user", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"Internal server error\"}");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || !pathInfo.matches("/\\d+")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"Invalid path for PUT\"}");
            return;
        }

        String idStr = pathInfo.substring(1);
        Long id;
        try {
            id = Long.parseLong(idStr);
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"Invalid ID format\"}");
            return;
        }

        UserDTO userDto = readJsonRequest(req, UserDTO.class);
        if (userDto == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"Invalid request body\"}");
            return;
        }

        userDto.setId(id);

        try {
            Optional<UserDTO> existingUser = userDao.findById(id);
            if (existingUser.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"error\": \"User not found\"}");
                return;
            }


            if (userDto.getRole() != null && !userDto.getRole().equals(existingUser.get().getRole())) {
                userDao.updateRole(id, userDto.getRole());
            }
            if (userDto.getPasswordHash() != null && !userDto.getPasswordHash().equals(existingUser.get().getPasswordHash())) {
                userDao.updatePassword(id, userDto.getPasswordHash());
            }

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write("{\"message\": \"User updated successfully\"}");
        } catch (Exception e) {
            logger.error("Error updating user with id=" + id, e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"Internal server error\"}");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || !pathInfo.matches("/\\d+")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"Invalid path for DELETE\"}");
            return;
        }

        String idStr = pathInfo.substring(1);
        Long id;
        try {
            id = Long.parseLong(idStr);
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"Invalid ID format\"}");
            return;
        }

        try {
            userDao.deleteById(id);
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (Exception e) {
            logger.error("Error deleting user with id=" + id, e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"Internal server error\"}");
        }
    }

    // Вспомогательные методы
    private <T> T readJsonRequest(HttpServletRequest req, Class<T> clazz) throws IOException {
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = req.getReader().readLine()) != null) {
            sb.append(line);
        }
        if (sb.length() == 0) {
            return null;
        }
        return objectMapper.readValue(sb.toString(), clazz);
    }

    private void writeJsonResponse(HttpServletResponse resp, Object data) throws IOException {
        PrintWriter writer = resp.getWriter();
        objectMapper.writeValue(writer, data);
        writer.flush();
    }

    private javax.sql.DataSource getDataSource() {
        try {
            javax.naming.Context initCtx = new javax.naming.InitialContext();
            javax.naming.Context envCtx = (javax.naming.Context) initCtx.lookup("java:comp/env");
            return (javax.sql.DataSource) envCtx.lookup("jdbc/YourAppDB");
        } catch (Exception e) {
            throw new RuntimeException("Failed to lookup DataSource", e);
        }
    }
}
