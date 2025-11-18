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

import javax.sql.DataSource;

@WebServlet("/api/v1/functions/*")
public class TabulatedFunctionServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(TabulatedFunctionServlet.class);
    private final TabulatedFunctionDao tabulatedFunctionDao = new TabulatedFunctionDaoImpl(getDataSource());
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final TabulatedFunctionFactory factory = new ArrayTabulatedFunctionFactory();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        logger.info("GET /api/v1/functions{} | params={} | remoteAddr={}",
                pathInfo, req.getParameterMap(), req.getRemoteAddr());
        resp.setContentType("application/json; charset=UTF-8");

        if (pathInfo == null || pathInfo.equals("/")) {
            // GET /api/functions?ownerId=... - получить все функции пользователя
            String ownerIdParam = req.getParameter("ownerId");
            if (ownerIdParam == null) {
                logger.warn("Missing 'ownerId' parameter in GET /");
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\": \"ownerId parameter is required\"}");
                return;
            }
            try {
                Long ownerId = Long.parseLong(ownerIdParam);
                logger.debug("Fetching functions for owner: {}", ownerId);
                List<TabulatedFunctionDTO> functions = tabulatedFunctionDao.findByOwnerId(ownerId);
                logger.info("Retrieved {} functions for owner {}", functions.size(), ownerId);
                writeJsonResponse(resp, functions);
            } catch (NumberFormatException e) {
                logger.warn("Invalid ownerId format: '{}'", ownerIdParam);
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\": \"Invalid ownerId format\"}");
            }
        } else {
            // GET /api/functions/{id}?ownerId= - получить функцию по ID
            String idStr = pathInfo.substring(1);
            String ownerIdParam = req.getParameter("ownerId");
            if (ownerIdParam == null) {
                logger.warn("Missing 'ownerId' parameter in GET /{}", idStr);
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\": \"ownerId parameter is required\"}");
                return;
            }
            try {
                Long id = Long.parseLong(idStr);
                Long ownerId = Long.parseLong(ownerIdParam);
                logger.debug("Fetching function: id={}, owner={}", id, ownerId);
                Optional<TabulatedFunctionDTO> funcOpt = tabulatedFunctionDao.findByIdAndOwnerId(id, ownerId);
                if (funcOpt.isPresent()) {
                    logger.info("Function found: id={}, owner={}", id, ownerId);
                    writeJsonResponse(resp, funcOpt.get());
                } else {
                    logger.warn("Function not found or access denied: id={}, owner={}", id, ownerId);
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    resp.getWriter().write("{\"error\": \"Function not found or access denied\"}");
                }
            } catch (NumberFormatException e) {
                logger.warn("Invalid ID or ownerId format: id='{}', ownerId='{}'", idStr, ownerIdParam);
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\": \"Invalid ID or ownerId format\"}");
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        logger.info("POST /api/v1/functions | remoteAddr={}", req.getRemoteAddr());
        resp.setContentType("application/json; charset=UTF-8");
        TabulatedFunctionDTO dto = readJsonRequest(req, TabulatedFunctionDTO.class);

        if (dto == null) {
            logger.warn("Invalid or empty request body in POST");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"Invalid request body\"}");
            return;
        }

        // Проверка обязательных полей
        if (dto.getOwnerId() == null || dto.getName() == null || dto.getSerializedData() == null) {
            logger.warn("Missing required fields in POST: ownerId={}, name={}, serializedData={}",
                    dto.getOwnerId(), dto.getName(), dto.getSerializedData() != null);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"ownerId, name and serialized_data are required\"}");
            return;
        }

        try {
            logger.debug("Saving new function: name='{}', owner={}", dto.getName(), dto.getOwnerId());
            Long savedId = tabulatedFunctionDao.save(dto);
            dto.setId(savedId);
            logger.info("Function created: id={}, owner={}", savedId, dto.getOwnerId());
            resp.setStatus(HttpServletResponse.SC_CREATED);
            writeJsonResponse(resp, dto);
        } catch (Exception e) {
            logger.error("Error saving function for owner={}", dto.getOwnerId(), e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"Internal server error\"}");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        logger.info("PUT /api/v1/functions{} | params={} | remoteAddr={}",
                pathInfo, req.getParameterMap(), req.getRemoteAddr());
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
            logger.debug("Updating function: id={}", id);
        } catch (NumberFormatException e) {
            logger.warn("Invalid ID format in PUT: '{}'", idStr);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"Invalid ID format\"}");
            return;
        }

        String ownerIdParam = req.getParameter("ownerId");
        if (ownerIdParam == null) {
            logger.warn("Missing 'ownerId' parameter in PUT for id={}", id);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"ownerId parameter is required\"}");
            return;
        }
        Long ownerId;
        try {
            ownerId = Long.parseLong(ownerIdParam);
        } catch (NumberFormatException e) {
            logger.warn("Invalid ownerId format in PUT: '{}'", ownerIdParam);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"Invalid ownerId format\"}");
            return;
        }

        TabulatedFunctionDTO dto = readJsonRequest(req, TabulatedFunctionDTO.class);
        if (dto == null) {
            logger.warn("Invalid or empty request body in PUT (id={}, owner={})", id, ownerId);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"Invalid request body\"}");
            return;
        }

        try {
            Optional<TabulatedFunctionDTO> existingFunc = tabulatedFunctionDao.findByIdAndOwnerId(id, ownerId);
            if (existingFunc.isEmpty()) {
                logger.warn("Update attempted on non-owned or missing function: id={}, owner={}", id, ownerId);
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"error\": \"Function not found or access denied\"}");
                return;
            }

            logger.debug("Updating function name and data: id={}, owner={}", id, ownerId);
            tabulatedFunctionDao.updateFunctionAndName(id, ownerId, dto);
            logger.info("Function updated successfully: id={}, owner={}", id, ownerId);
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write("{\"message\": \"Function updated successfully\"}");
        } catch (Exception e) {
            logger.error("Error updating function with id={}, owner={}", id, ownerId, e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"Internal server error\"}");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        logger.info("DELETE /api/v1/functions{} | params={} | remoteAddr={}",
                pathInfo, req.getParameterMap(), req.getRemoteAddr());
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
            logger.debug("Deleting function: id={}", id);
        } catch (NumberFormatException e) {
            logger.warn("Invalid ID format in DELETE: '{}'", idStr);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"Invalid ID format\"}");
            return;
        }

        String ownerIdParam = req.getParameter("ownerId");
        if (ownerIdParam == null) {
            logger.warn("Missing 'ownerId' parameter in DELETE for id={}", id);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"ownerId parameter is required\"}");
            return;
        }
        Long ownerId;
        try {
            ownerId = Long.parseLong(ownerIdParam);
        } catch (NumberFormatException e) {
            logger.warn("Invalid ownerId format in DELETE: '{}'", ownerIdParam);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"Invalid ownerId format\"}");
            return;
        }

        try {
            logger.debug("Deleting function: id={}, owner={}", id, ownerId);
            tabulatedFunctionDao.deleteByIdAndOwnerId(id, ownerId);
            logger.info("Function deleted: id={}, owner={}", id, ownerId);
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (Exception e) {
            logger.error("Error deleting function with id={}, owner={}", id, ownerId, e);
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
            DataSource ds = (javax.sql.DataSource) envCtx.lookup("jdbc/lr-oop");
            logger.info("DataSource initialized successfully");
            return ds;
        } catch (Exception e) {
            logger.error("Failed to lookup DataSource", e);
            throw new RuntimeException("Failed to lookup DataSource", e);
        }
    }
}