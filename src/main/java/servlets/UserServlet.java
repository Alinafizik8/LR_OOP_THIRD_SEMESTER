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
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.sql.DataSource;

@WebServlet("/api/v1/users/*")
public class UserServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(UserServlet.class);
    private final UserDao userDao = new UserDaoImpl(getDataSource());
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        logger.info("GET /api/v1/users{} | remoteAddr={}", pathInfo, req.getRemoteAddr());
        resp.setContentType("application/json; charset=UTF-8");

        if (pathInfo == null || pathInfo.equals("/")) {
            // GET /api/users - получить всех пользователей
            logger.debug("Fetching all users");
            List<UserDTO> users = userDao.findAll();
            logger.info("Retrieved {} users", users.size());
            writeJsonResponse(resp, users);
        } else {
            // GET /api/users/{id} - получить пользователя по ID
            String idStr = pathInfo.substring(1); // убираем первый '/'
            logger.debug("Fetching user by ID: '{}'", idStr);
            try {
                Long id = Long.parseLong(idStr);
                Optional<UserDTO> userOpt = userDao.findById(id);
                if (userOpt.isPresent()) {
                    logger.info("User found: id={}", id);
                    writeJsonResponse(resp, userOpt.get());
                } else {
                    logger.warn("User not found: id={}", id);
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    resp.getWriter().write("{\"error\": \"User not found\"}");
                }
            } catch (NumberFormatException e) {
                logger.warn("Invalid ID format: '{}'", idStr);
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\": \"Invalid ID format\"}");
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        logger.info("POST /api/v1/users | remoteAddr={}", req.getRemoteAddr());
        resp.setContentType("application/json; charset=UTF-8");
        UserDTO userDto = readJsonRequest(req, UserDTO.class);

        if (userDto == null) {
            logger.warn("Invalid or empty request body in POST");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"Invalid request body\"}");
            return;
        }

        try {
            logger.debug("Saving new user: username='{}', email='{}'",
                    userDto.getUsername(), userDto.getEmail());
            Long savedId = userDao.save(userDto);
            userDto.setId(savedId);
            logger.info("User created: id={}", savedId);
            resp.setStatus(HttpServletResponse.SC_CREATED);
            writeJsonResponse(resp, userDto);
        } catch (Exception e) {
            logger.error("Error saving user: username='{}'", userDto.getUsername(), e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"Internal server error\"}");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        logger.info("PUT /api/v1/users{} | remoteAddr={}", pathInfo, req.getRemoteAddr());
        if (pathInfo == null || !pathInfo.matches("/\\d+")) {
            logger.warn("Invalid path for PUT: '{}'", pathInfo);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"Invalid path for PUT\"}");
            return;
        }

        String idStr = pathInfo.substring(1);
        Long id;
        try {
            id = Long.parseLong(idStr);
            logger.debug("Updating user: id={}", id);
        } catch (NumberFormatException e) {
            logger.warn("Invalid ID format in PUT: '{}'", idStr);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"Invalid ID format\"}");
            return;
        }

        UserDTO userDto = readJsonRequest(req, UserDTO.class);
        if (userDto == null) {
            logger.warn("Invalid or empty request body in PUT (id={})", id);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"Invalid request body\"}");
            return;
        }

        userDto.setId(id);

        try {
            Optional<UserDTO> existingUserOpt = userDao.findById(id);
            if (existingUserOpt.isEmpty()) {
                logger.warn("Update attempted on non-existent user: id={}", id);
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"error\": \"User not found\"}");
                return;
            }

            UserDTO existingUser = existingUserOpt.get();
            boolean changed = false;

            if (userDto.getRole() != null && !userDto.getRole().equals(existingUser.getRole())) {
                logger.info("Updating role for user {}: {} → {}", id, existingUser.getRole(), userDto.getRole());
                userDao.updateRole(id, userDto.getRole());
                changed = true;
            }
            if (userDto.getPasswordHash() != null && !userDto.getPasswordHash().equals(existingUser.getPasswordHash())) {
                logger.info("Updating password hash for user: id={}", id);
                userDao.updatePassword(id, userDto.getPasswordHash());
                changed = true;
            }

            if (changed) {
                logger.info("User updated: id={}", id);
            } else {
                logger.debug("PUT request for user {} contained no changes", id);
            }

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write("{\"message\": \"User updated successfully\"}");
        } catch (Exception e) {
            logger.error("Error updating user with id={}", id, e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"Internal server error\"}");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        logger.info("DELETE /api/v1/users{} | remoteAddr={}", pathInfo, req.getRemoteAddr());
        if (pathInfo == null || !pathInfo.matches("/\\d+")) {
            logger.warn("Invalid path for DELETE: '{}'", pathInfo);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"Invalid path for DELETE\"}");
            return;
        }

        String idStr = pathInfo.substring(1);
        Long id;
        try {
            id = Long.parseLong(idStr);
            logger.debug("Deleting user: id={}", id);
        } catch (NumberFormatException e) {
            logger.warn("Invalid ID format in DELETE: '{}'", idStr);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"Invalid ID format\"}");
            return;
        }

        try {
            Optional<UserDTO> userOpt = userDao.findById(id);
            if (userOpt.isPresent()) {
                logger.info("Deleting user: id={}", id);
                userDao.deleteById(id);
                resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
            } else {
                logger.warn("Delete attempted on non-existent user: id={}", id);
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"error\": \"User not found\"}");
            }
        } catch (Exception e) {
            logger.error("Error deleting user with id={}", id, e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"Internal server error\"}");
        }
    }

    // Вспомогательные методы
    private <T> T readJsonRequest(HttpServletRequest req, Class<T> clazz) throws IOException {
        logger.trace("Reading JSON request body");
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = req.getReader().readLine()) != null) {
            sb.append(line);
        }
        if (sb.length() == 0) {
            logger.debug("Empty request body");
            return null;
        }
        try {
            T result = objectMapper.readValue(sb.toString(), clazz);
            logger.trace("JSON parsed successfully");
            return result;
        } catch (Exception e) {
            logger.warn("Failed to parse JSON request", e);
            return null;
        }
    }

    private void writeJsonResponse(HttpServletResponse resp, Object data) throws IOException {
        logger.trace("Writing JSON response");
        PrintWriter writer = resp.getWriter();
        objectMapper.writeValue(writer, data);
        writer.flush();
        logger.trace("JSON response sent");
    }

    private javax.sql.DataSource getDataSource() {
        try {
            javax.naming.Context initCtx = new javax.naming.InitialContext();
            javax.naming.Context envCtx = (javax.naming.Context) initCtx.lookup("java:comp/env");
            DataSource ds = (javax.sql.DataSource) envCtx.lookup("jdbc/LR_OOP_THIRD_SEMESTER");
            logger.info("DataSource initialized successfully");
            return ds;
        } catch (Exception e) {
            logger.error("Failed to lookup DataSource", e);
            throw new RuntimeException("Failed to lookup DataSource", e);
        }
    }
}
