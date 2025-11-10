package functions.dao;

import functions.dto.TabulatedFunctionDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO для таблицы tabulated_functions.
 * Реализует CRUD + поиск по ownerId + изоляцию.
 * Использует запросы из crud_functions.sql.
 */
public class TabulatedFunctionDao {
    private final DataSource dataSource;
    private final Logger logger = LoggerFactory.getLogger(TabulatedFunctionDao.class);

    public TabulatedFunctionDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // ========== CREATE ========== //
    public Long save(TabulatedFunctionDTO function) {
        logger.info("Saving function '{}' for owner {}", function.getName(), function.getOwnerId());
        String sql = """
            INSERT INTO tabulated_functions (owner_id, function_type_id, serialized_data, name)
            VALUES (?, ?, ?, ?) RETURNING id
            """;
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, function.getOwnerId());
                ps.setLong(2, function.getFunctionTypeId());
                ps.setBytes(3, function.getSerializedData());
                ps.setString(4, function.getName());

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        Long id = rs.getLong("id");
                        conn.commit();
                        logger.debug("Function saved: id={} name='{}'", id, function.getName());
                        return id;
                    }
                }
            } catch (SQLException e) {
                conn.rollback();
                logger.error("Save failed (owner={}, name={})", function.getOwnerId(), function.getName(), e);
                throw new RuntimeException("Save failed", e);
            }
        } catch (SQLException e) {
            logger.error("DB connection error", e);
            throw new RuntimeException("DB error", e);
        }
        throw new RuntimeException("Insert returned no ID");
    }

    // ========== READ (single) ========== //
    public Optional<TabulatedFunctionDTO> findByIdAndOwnerId(Long id, Long ownerId) {
        logger.debug("Finding function id={} for owner={}", id, ownerId);
        String sql = "SELECT * FROM tabulated_functions WHERE id = ? AND owner_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.setLong(2, ownerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    TabulatedFunctionDTO dto = mapResultSet(rs);
                    logger.trace("Found: {}", dto);
                    return Optional.of(dto);
                } else {
                    logger.debug("Not found: id={} owner={}", id, ownerId);
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            logger.error("Find failed: id={} owner={}", id, ownerId, e);
            throw new RuntimeException("Find failed", e);
        }
    }

    // ========== READ (list) ========== //
    public List<TabulatedFunctionDTO> findByOwnerId(Long ownerId) {
        logger.debug("Loading all functions for owner={}", ownerId);
        String sql = "SELECT * FROM tabulated_functions WHERE owner_id = ? ORDER BY created_at DESC";
        List<TabulatedFunctionDTO> list = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, ownerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Find all failed for owner={}", ownerId, e);
            throw new RuntimeException("Find all failed", e);
        }
        logger.info("Loaded {} functions for owner {}", list.size(), ownerId);
        return list;
    }

    // ========== UPDATE ========== //
    public void updateName(Long id, Long ownerId, String newName) {
        logger.info("✏Updating name of function {} → '{}'", id, newName);
        String sql = "UPDATE tabulated_functions SET name = ?, updated_at = NOW() WHERE id = ? AND owner_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newName);
            ps.setLong(2, id);
            ps.setLong(3, ownerId);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                logger.warn("Update skipped: function {} not found for owner {}", id, ownerId);
                throw new RuntimeException("Function not found");
            }
            logger.debug("Name updated for function {}", id);
        } catch (SQLException e) {
            logger.error("Update name failed: id={}", id, e);
            throw new RuntimeException("Update failed", e);
        }
    }

    public void updateDataAndName(Long id, Long ownerId, TabulatedFunctionDTO newData) {
        logger.info("Updating data & name of function {}", id);
        String sql = """
            UPDATE tabulated_functions
            SET serialized_data = ?, name = ?, updated_at = NOW()
            WHERE id = ? AND owner_id = ?
            """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBytes(1, newData.getSerializedData());
            ps.setString(2, newData.getName());
            ps.setLong(3, id);
            ps.setLong(4, ownerId);
            int rows = ps.executeUpdate();
            if (rows == 0) throw new RuntimeException("Function not found");
        } catch (SQLException e) {
            logger.error("Update data failed: id={}", id, e);
            throw new RuntimeException("Update data failed", e);
        }
    }

    // ========== DELETE ========== //
    public void deleteByIdAndOwnerId(Long id, Long ownerId) {
        logger.info("🗑️  Deleting function {} for owner {}", id, ownerId);
        String sql = "DELETE FROM tabulated_functions WHERE id = ? AND owner_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.setLong(2, ownerId);
            int rows = ps.executeUpdate();
            logger.debug("Deleted {} rows (id={}, owner={})", rows, id, ownerId);
        } catch (SQLException e) {
            logger.error("Delete failed: id={} owner={}", id, ownerId, e);
            throw new RuntimeException("Delete failed", e);
        }
    }

    // ========== Helper ========== //
    private TabulatedFunctionDTO mapResultSet(ResultSet rs) throws SQLException {
        TabulatedFunctionDTO dto = new TabulatedFunctionDTO();
        dto.setId(rs.getLong("id"));
        dto.setOwnerId(rs.getLong("owner_id"));
        dto.setFunctionTypeId(rs.getLong("function_type_id"));
        dto.setSerializedData(rs.getBytes("serialized_data"));
        dto.setName(rs.getString("name"));
        dto.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        dto.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return dto;
    }
}
