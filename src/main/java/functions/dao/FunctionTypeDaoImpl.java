package functions.dao;

import dto.FunctionTypeDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FunctionTypeDaoImpl implements FunctionTypeDao {
    private final DataSource dataSource;
    private final Logger logger = LoggerFactory.getLogger(FunctionTypeDaoImpl.class);

    public FunctionTypeDaoImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Long save(FunctionTypeDTO type) {
        logger.info("Saving function type: '{}'", type.getName());
        String sql = """
        INSERT INTO function_types (name, localized_name, priority)
        VALUES (?, ?, ?)
        """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) { // ← Получаем сгенерированный ID
            ps.setString(1, type.getName());
            ps.setString(2, type.getLocalizedName());
            ps.setInt(3, type.getPriority());

            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new RuntimeException("Insert failed");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Long id = generatedKeys.getLong(1);
                    logger.debug("✅ Saved: id={}, name='{}'", id, type.getName());
                    return id;
                }
            }
        } catch (SQLException e) {
            logger.error("Save failed for function type '{}'", type.getName(), e);
            throw new RuntimeException("Failed to save function type", e);
        }
        throw new RuntimeException("Insert returned no ID");
    }

    @Override
    public Optional<FunctionTypeDTO> findById(Long id) {
        logger.debug("Finding function type by id={}", id);
        String sql = "SELECT * FROM function_types WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return extractSingle(ps);
        } catch (SQLException e) {
            logger.error("Find by id={} failed", id, e);
            throw new RuntimeException("Find by ID failed", e);
        }
    }

    @Override
    public Optional<FunctionTypeDTO> findByName(String name) {
        logger.debug("Finding function type by name='{}'", name);
        String sql = "SELECT * FROM function_types WHERE name = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            return extractSingle(ps);
        } catch (SQLException e) {
            logger.error("Find by name='{}' failed", name, e);
            throw new RuntimeException("Find by name failed", e);
        }
    }

    @Override
    public List<FunctionTypeDTO> findAll() {
        logger.debug("Loading all function types");
        String sql = "SELECT * FROM function_types ORDER BY id";
        return extractList(sql);
    }

    @Override
    public List<FunctionTypeDTO> findAllSortedByPriority() {
        logger.debug("Loading function types sorted by priority");
        String sql = """
            SELECT * FROM function_types 
            ORDER BY priority ASC, localized_name ASC
            """;
        return extractList(sql);
    }

    @Override
    public void update(FunctionTypeDTO type) {
        logger.info("Updating function type: id={}, name='{}'", type.getId(), type.getName());
        String sql = """
            UPDATE function_types 
            SET name = ?, localized_name = ?, priority = ?, updated_at = NOW()
            WHERE id = ?
            """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, type.getName());
            ps.setString(2, type.getLocalizedName());
            ps.setInt(3, type.getPriority());
            ps.setLong(4, type.getId());

            int rows = ps.executeUpdate();
            if (rows == 0) {
                logger.warn("Update skipped: function type id={} not found", type.getId());
                throw new RuntimeException("FunctionType not found");
            }
            logger.debug("Updated function type id={}", type.getId());
        } catch (SQLException e) {
            logger.error("Update failed for function type id={}", type.getId(), e);
            throw new RuntimeException("Update failed", e);
        }
    }

    @Override
    public void deleteById(Long id) {
        logger.info("Deleting function type id={}", id);
        String sql = "DELETE FROM function_types WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            int rows = ps.executeUpdate();
            logger.debug("Deleted {} row(s) for id={}", rows, id);
        } catch (SQLException e) {
            logger.error("Delete failed for function type id={}", id, e);
            throw new RuntimeException("Delete failed", e);
        }
    }

    // ===== Вспомогательные методы =====
    private Optional<FunctionTypeDTO> extractSingle(PreparedStatement ps) throws SQLException {
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                FunctionTypeDTO dto = map(rs);
                logger.trace("Found: {}", dto);
                return Optional.of(dto);
            } else {
                logger.debug("Not found");
                return Optional.empty();
            }
        }
    }

    private List<FunctionTypeDTO> extractList(String sql) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<FunctionTypeDTO> list = new ArrayList<>();
            while (rs.next()) {
                list.add(map(rs));
            }
            logger.info("Loaded {} function types", list.size());
            return list;
        } catch (SQLException e) {
            logger.error("Query failed: {}", sql, e);
            throw new RuntimeException("Query failed", e);
        }
    }

    private FunctionTypeDTO map(ResultSet rs) throws SQLException {
        return new FunctionTypeDTO(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("localized_name"),
                rs.getInt("priority"),
                rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getTimestamp("updated_at").toLocalDateTime()
        );
    }
}