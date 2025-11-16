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

@WebServlet("/api/function-types/*")
public class FunctionTypeServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(FunctionTypeServlet.class);
    private final FunctionTypeDao functionTypeDao = new FunctionTypeDaoImpl(getDataSource());
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        resp.setContentType("application/json; charset=UTF-8");

        if (pathInfo == null || pathInfo.equals("/")) {
            // GET /api/function-types - получить все типы функций
            List<FunctionTypeDTO> types = functionTypeDao.findAllSortedByPriority();
            writeJsonResponse(resp, types);
        } else {
            // GET /api/function-types/{id} - получить тип функции по ID
            String idStr = pathInfo.substring(1);
            try {
                Long id = Long.parseLong(idStr);
                Optional<FunctionTypeDTO> typeOpt = functionTypeDao.findById(id);
                if (typeOpt.isPresent()) {
                    writeJsonResponse(resp, typeOpt.get());
                } else {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    resp.getWriter().write("{\"error\": \"Function type not found\"}");
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
        FunctionTypeDTO typeDto = readJsonRequest(req, FunctionTypeDTO.class);

        if (typeDto == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"Invalid request body\"}");
            return;
        }

        try {
            Long savedId = functionTypeDao.save(typeDto);
            typeDto.setId(savedId);
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

        FunctionTypeDTO typeDto = readJsonRequest(req, FunctionTypeDTO.class);
        if (typeDto == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"Invalid request body\"}");
            return;
        }

        typeDto.setId(id);

        try {
            Optional<FunctionTypeDTO> existingType = functionTypeDao.findById(id);
            if (existingType.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"error\": \"Function type not found\"}");
                return;
            }

            functionTypeDao.update(typeDto);
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write("{\"message\": \"Function type updated successfully\"}");
        } catch (Exception e) {
            logger.error("Error updating function type with id=" + id, e);
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
            functionTypeDao.deleteById(id);
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (Exception e) {
            logger.error("Error deleting function type with id=" + id, e);
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
