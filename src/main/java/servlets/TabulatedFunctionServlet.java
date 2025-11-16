package servlets;

import dto.TabulatedFunctionDTO;
import functions.factory.TabulatedFunctionFactory;
import functions.factory.ArrayTabulatedFunctionFactory;
import functions.dao.TabulatedFunctionDao;
import functions.dao.TabulatedFunctionDaoImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;

@WebServlet("/api/functions/*")
public class TabulatedFunctionServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(TabulatedFunctionServlet.class);
    private final TabulatedFunctionDao tabulatedFunctionDao = new TabulatedFunctionDaoImpl(getDataSource());
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final TabulatedFunctionFactory factory = new ArrayTabulatedFunctionFactory();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        resp.setContentType("application/json; charset=UTF-8");

        if (pathInfo == null || pathInfo.equals("/")) {
            // GET /api/functions?ownerId=... - получить все функции пользователя
            String ownerIdParam = req.getParameter("ownerId");
            if (ownerIdParam == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\": \"ownerId parameter is required\"}");
                return;
            }
            try {
                Long ownerId = Long.parseLong(ownerIdParam);
                List<TabulatedFunctionDTO> functions = tabulatedFunctionDao.findByOwnerId(ownerId);
                writeJsonResponse(resp, functions);
            } catch (NumberFormatException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\": \"Invalid ownerId format\"}");
            }
        } else {
            // GET /api/functions/{id}?ownerId= - получить функцию по ID
            String idStr = pathInfo.substring(1);
            String ownerIdParam = req.getParameter("ownerId");
            if (ownerIdParam == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\": \"ownerId parameter is required\"}");
                return;
            }
            try {
                Long id = Long.parseLong(idStr);
                Long ownerId = Long.parseLong(ownerIdParam);
                Optional<TabulatedFunctionDTO> funcOpt = tabulatedFunctionDao.findByIdAndOwnerId(id, ownerId);
                if (funcOpt.isPresent()) {
                    writeJsonResponse(resp, funcOpt.get());
                } else {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    resp.getWriter().write("{\"error\": \"Function not found or access denied\"}");
                }
            } catch (NumberFormatException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\": \"Invalid ID or ownerId format\"}");
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json; charset=UTF-8");
        TabulatedFunctionDTO dto = readJsonRequest(req, TabulatedFunctionDTO.class);

        if (dto == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"Invalid request body\"}");
            return;
        }

        // Проверка обязательных полей
        if (dto.getOwnerId() == null || dto.getName() == null || dto.getSerializedData() == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"ownerId, name and serialized_data are required\"}");
            return;
        }

        try {
            Long savedId = tabulatedFunctionDao.save(dto);
            dto.setId(savedId);
            resp.setStatus(HttpServletResponse.SC_CREATED);
            writeJsonResponse(resp, dto);
        } catch (Exception e) {
            logger.error("Error saving function", e);
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

        String ownerIdParam = req.getParameter("ownerId");
        if (ownerIdParam == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"ownerId parameter is required\"}");
            return;
        }
        Long ownerId;
        try {
            ownerId = Long.parseLong(ownerIdParam);
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"Invalid ownerId format\"}");
            return;
        }

        TabulatedFunctionDTO dto = readJsonRequest(req, TabulatedFunctionDTO.class);
        if (dto == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"Invalid request body\"}");
            return;
        }

        try {
            Optional<TabulatedFunctionDTO> existingFunc = tabulatedFunctionDao.findByIdAndOwnerId(id, ownerId);
            if (existingFunc.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"error\": \"Function not found or access denied\"}");
                return;
            }

            tabulatedFunctionDao.updateFunctionAndName(id, ownerId, dto);
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write("{\"message\": \"Function updated successfully\"}");
        } catch (Exception e) {
            logger.error("Error updating function with id=" + id, e);
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

        String ownerIdParam = req.getParameter("ownerId");
        if (ownerIdParam == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"ownerId parameter is required\"}");
            return;
        }
        Long ownerId;
        try {
            ownerId = Long.parseLong(ownerIdParam);
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"Invalid ownerId format\"}");
            return;
        }

        try {
            tabulatedFunctionDao.deleteByIdAndOwnerId(id, ownerId);
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (Exception e) {
            logger.error("Error deleting function with id=" + id, e);
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
