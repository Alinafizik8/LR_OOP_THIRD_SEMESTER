package servlets;

import dto.FunctionTypeDTO;
import functions.dao.FunctionTypeDao;
import functions.dao.FunctionTypeDaoImpl;
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

import javax.sql.DataSource;

@WebServlet("/api/v1/function-types/*")
public class FunctionTypeServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(FunctionTypeServlet.class);
    private final FunctionTypeDao functionTypeDao = new FunctionTypeDaoImpl(getDataSource());
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        logger.info("GET /api/v1/function-types{} | remoteAddr={}", pathInfo, req.getRemoteAddr());
        resp.setContentType("application/json; charset=UTF-8");

        if (pathInfo == null || pathInfo.equals("/")) {
            // GET /api/function-types - получить все типы функций
            logger.debug("Fetching all function types (sorted by priority)");
            List<FunctionTypeDTO> types = functionTypeDao.findAllSortedByPriority();
            logger.info("Retrieved {} function types", types.size());
            writeJsonResponse(resp, types);
        } else {
            // GET /api/function-types/{id} - получить тип функции по ID
            String idStr = pathInfo.substring(1);
            logger.debug("Fetching function type by ID: '{}'", idStr);
            try {
                Long id = Long.parseLong(idStr);
                Optional<FunctionTypeDTO> typeOpt = functionTypeDao.findById(id);
                if (typeOpt.isPresent()) {
                    logger.info("Function type found: id={}", id);
                    writeJsonResponse(resp, typeOpt.get());
                } else {
                    logger.warn("Function type not found: id={}", id);
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    resp.getWriter().write("{\"error\": \"Function type not found\"}");
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
        logger.info("POST /api/v1/function-types | remoteAddr={}", req.getRemoteAddr());
        resp.setContentType("application/json; charset=UTF-8");
        FunctionTypeDTO typeDto = readJsonRequest(req, FunctionTypeDTO.class);

        if (typeDto == null) {
            logger.warn("Invalid or empty request body in POST");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"Invalid request body\"}");
            return;
        }

        try {
            logger.debug("Saving new function type: name='{}'", typeDto.getName());
            Long savedId = functionTypeDao.save(typeDto);
            typeDto.setId(savedId);
            logger.info("Function type created: id={}", savedId);
            resp.setStatus(HttpServletResponse.SC_CREATED);
            writeJsonResponse(resp, typeDto);
        } catch (Exception e) {
            logger.error("Error saving function type", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"Internal server error\"}");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        logger.info("PUT /api/v1/function-types{} | remoteAddr={}", pathInfo, req.getRemoteAddr());
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
            logger.debug("Updating function type with id={}", id);
        } catch (NumberFormatException e) {
            logger.warn("Invalid ID format in PUT: '{}'", idStr);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"Invalid ID format\"}");
            return;
        }

        FunctionTypeDTO typeDto = readJsonRequest(req, FunctionTypeDTO.class);
        if (typeDto == null) {
            logger.warn("Invalid or empty request body in PUT (id={})", id);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"Invalid request body\"}");
            return;
        }

        typeDto.setId(id);

        try {
            Optional<FunctionTypeDTO> existingType = functionTypeDao.findById(id);
            if (existingType.isEmpty()) {
                logger.warn("Update attempted on non-existent function type: id={}", id);
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"error\": \"Function type not found\"}");
                return;
            }

            functionTypeDao.update(typeDto);
            logger.info("Function type updated successfully: id={}", id);
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write("{\"message\": \"Function type updated successfully\"}");
        } catch (Exception e) {
            logger.error("Error updating function type with id={}", id, e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"Internal server error\"}");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        logger.info("DELETE /api/v1/function-types{} | remoteAddr={}", pathInfo, req.getRemoteAddr());
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
            logger.debug("Deleting function type with id={}", id);
        } catch (NumberFormatException e) {
            logger.warn("Invalid ID format in DELETE: '{}'", idStr);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"Invalid ID format\"}");
            return;
        }

        try {
            functionTypeDao.deleteById(id);
            logger.info("Function type deleted: id={}", id);
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (Exception e) {
            logger.error("Error deleting function type with id={}", id, e);
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
            DataSource ds = (javax.sql.DataSource) envCtx.lookup("jdbc/YourAppDB");
            logger.info("DataSource initialized successfully");
            return ds;
        } catch (Exception e) {
            logger.error("Failed to lookup DataSource", e);
            throw new RuntimeException("Failed to lookup DataSource", e);
        }
    }
}