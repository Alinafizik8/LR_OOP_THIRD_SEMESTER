package functions.dao;

import dto.TabulatedFunctionDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TabulatedFunctionDaoImpl implements TabulatedFunctionDao {
    private final DataSource dataSource;
    private final Logger logger = LoggerFactory.getLogger(TabulatedFunctionDaoImpl.class);

    public TabulatedFunctionDaoImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Long save(TabulatedFunctionDTO dto) {
        logger.info("Saving function '{}' for owner id={}", dto.getName(), dto.getOwnerId());
        String sql = """
        INSERT INTO tabulated_functions (owner_id, function_type_id, serialized_data, name)
        VALUES (?, ?, ?, ?)
        """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) { // ← Получаем сгенерированный ID
            ps.setLong(1, dto.getOwnerId());
            ps.setLong(2, dto.getFunctionTypeId());
            ps.setBytes(3, dto.getSerializedData());
            ps.setString(4, dto.getName());

            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new RuntimeException("Insert failed");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Long id = generatedKeys.getLong(1);
                    logger.debug("Saved: id={}, name='{}'", id, dto.getName());
                    return id;
                }
            }
        } catch (SQLException e) {
            logger.error("Save failed for function '{}' (owner={})", dto.getName(), dto.getOwnerId(), e);
            throw new RuntimeException("Save failed", e);
        }
        throw new RuntimeException("Insert returned no ID");
    }

    @Override
    public Optional<TabulatedFunctionDTO> findByIdAndOwnerId(Long id, Long ownerId) {
        logger.debug("Finding function id={} for owner id={}", id, ownerId);
        String sql = "SELECT * FROM tabulated_functions WHERE id = ? AND owner_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.setLong(2, ownerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    TabulatedFunctionDTO dto = TabulatedFunctionDTO.fromResultSet(rs);
                    logger.trace("Found: {}", dto);
                    return Optional.of(dto);
                } else {
                    logger.debug("Not found: id={}, owner={}", id, ownerId);
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            logger.error("Find failed: id={}, owner_id={}", id, ownerId, e);
            throw new RuntimeException("Find failed", e);
        }
    }

    @Override
    public List<TabulatedFunctionDTO> findByOwnerId(Long ownerId) {
        logger.debug("Loading all functions for owner id={}", ownerId);
        String sql = "SELECT * FROM tabulated_functions WHERE owner_id = ? ORDER BY created_at DESC";
        return extractList(sql, ownerId);
    }

    @Override
    public List<TabulatedFunctionDTO> findByOwnerIdAndTypeId(Long ownerId, Long typeId) {
        logger.debug("Loading functions for owner id={} and type id={}", ownerId, typeId);
        String sql = "SELECT * FROM tabulated_functions WHERE owner_id = ? AND function_type_id = ? ORDER BY name ASC";
        return extractListWithParams(sql, ownerId, typeId);
    }

    @Override
    public void updateName(Long id, Long ownerId, String newName) {
        logger.info("Updating name of function id={} → '{}'", id, newName);
        String sql = "UPDATE tabulated_functions SET name = ?, updated_at = NOW() WHERE id = ? AND owner_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newName);
            ps.setLong(2, id);
            ps.setLong(3, ownerId);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                logger.warn("Update skipped: function id={} not found for owner id={}", id, ownerId);
                throw new RuntimeException("Function not found or access denied");
            }
            logger.debug("Name updated for function id={}", id);
        } catch (SQLException e) {
            logger.error("Update name failed: id={}, owner_id={}", id, ownerId, e);
            throw new RuntimeException("Update name failed", e);
        }
    }

    @Override
    public void updateFunctionAndName(Long id, Long ownerId, TabulatedFunctionDTO newDto) {
        logger.info("Updating function & name for id={}", id);
        String sql = """
            UPDATE tabulated_functions 
            SET serialized_data = ?, name = ?, updated_at = NOW() 
            WHERE id = ? AND owner_id = ?
            """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBytes(1, newDto.getSerializedData());
            ps.setString(2, newDto.getName());
            ps.setLong(3, id);
            ps.setLong(4, ownerId);

            int rows = ps.executeUpdate();
            if (rows == 0) {
                logger.warn("Update skipped: function id={} not found for owner id={}", id, ownerId);
                throw new RuntimeException("Function not found or access denied");
            }
            logger.debug("Function & name updated for id={}", id);
        } catch (SQLException e) {
            logger.error("Update function failed: id={}, owner_id={}", id, ownerId, e);
            throw new RuntimeException("Update function failed", e);
        }
    }

    @Override
    public void deleteByIdAndOwnerId(Long id, Long ownerId) {
        logger.info("Deleting function id={} for owner id={}", id, ownerId);
        String sql = "DELETE FROM tabulated_functions WHERE id = ? AND owner_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.setLong(2, ownerId);
            int rows = ps.executeUpdate();
            logger.debug("Deleted {} row(s) (id={}, owner={})", rows, id, ownerId);
        } catch (SQLException e) {
            logger.error("Delete failed: id={}, owner_id={}", id, ownerId, e);
            throw new RuntimeException("Delete failed", e);
        }
    }

    @Override
    public List<TabulatedFunctionDTO> findByOwnerIdSortedByNameAsc(Long ownerId) {
        logger.debug("Searching functions for owner id={} sorted by name ASC", ownerId);
        String sql = """
        SELECT * FROM tabulated_functions 
        WHERE owner_id = ? 
        ORDER BY name ASC
        """;
        return extractList(sql, ownerId);
    }

    @Override
    public List<TabulatedFunctionDTO> findByOwnerIdSortedByCreatedAtDesc(Long ownerId) {
        logger.debug("Searching functions for owner id={} sorted by created_at DESC", ownerId);
        String sql = """
        SELECT * FROM tabulated_functions 
        WHERE owner_id = ? 
        ORDER BY created_at DESC
        """;
        return extractList(sql, ownerId);
    }

    @Override
    public List<TabulatedFunctionDTO> findByOwnerIdAndTypeIdSortedByNameAsc(Long ownerId, Long typeId) {
        logger.debug("Searching functions for owner id={} and type id={} sorted by name ASC", ownerId, typeId);
        String sql = """
        SELECT * FROM tabulated_functions 
        WHERE owner_id = ? AND function_type_id = ? 
        ORDER BY name ASC
        """;
        return extractListWithParams(sql, ownerId, typeId);
    }

    @Override
    public List<TabulatedFunctionDTO> findByOwnerIdAndTypeIdSortedByCreatedAtDesc(Long ownerId, Long typeId) {
        logger.debug("Searching functions for owner id={} and type id={} sorted by created_at DESC", ownerId, typeId);
        String sql = """
        SELECT * FROM tabulated_functions 
        WHERE owner_id = ? AND function_type_id = ? 
        ORDER BY created_at DESC
        """;
        return extractListWithParams(sql, ownerId, typeId);
    }

    @Override
    public List<TabulatedFunctionDTO> findByOwnerIdAndNameContaining(Long ownerId, String nameFragment) {
        logger.info("Full-text search: owner={} name LIKE '%{}%'", ownerId, nameFragment);
        String sql = """
        SELECT * FROM tabulated_functions 
        WHERE owner_id = ? AND LOWER(name) LIKE LOWER(?)
        ORDER BY created_at DESC
        """;
        String pattern = "%" + nameFragment + "%";
        return extractListWithParams(sql, ownerId, pattern);
    }

    private List<TabulatedFunctionDTO> extractListWithParams(String sql, Object... params) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            return extractFromResultSet(ps);
        } catch (SQLException e) {
            logger.error("Parameterized query failed: {}", sql, e);
            throw new RuntimeException("Query failed", e);
        }
    }

    // ===== Вспомогательные методы =====
    private List<TabulatedFunctionDTO> extractList(String sql, Long param) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, param);
            return extractFromResultSet(ps);
        } catch (SQLException e) {
            logger.error("Query failed: {}", sql, e);
            throw new RuntimeException("Query failed", e);
        }
    }

    private List<TabulatedFunctionDTO> extractListWithParam(String sql, Long p1, Long p2) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, p1);
            ps.setLong(2, p2);
            return extractFromResultSet(ps);
        } catch (SQLException e) {
            logger.error("Query failed: {}", sql, e);
            throw new RuntimeException("Query failed", e);
        }
    }

    private List<TabulatedFunctionDTO> extractFromResultSet(PreparedStatement ps) throws SQLException {
        try (ResultSet rs = ps.executeQuery()) {
            List<TabulatedFunctionDTO> list = new ArrayList<>();
            while (rs.next()) {
                list.add(TabulatedFunctionDTO.fromResultSet(rs));
            }
            logger.info("Loaded {} functions", list.size());
            return list;
        }
    }
}