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

public class TabulatedFunctionDaoImpl implements TabulatedFunctionDao {
    private final DataSource dataSource;
    private final Logger logger = LoggerFactory.getLogger(TabulatedFunctionDaoImpl.class);

    public TabulatedFunctionDaoImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Long save(TabulatedFunctionDTO function) {
        logger.debug("[SAVE] Сохранение функции '{}' для пользователя {}", function.getName(), function.getOwnerId());
        String sql = """
        INSERT INTO tabulated_functions (owner_id, function_type_id, serialized_data, name, created_at, updated_at) 
        VALUES (?, ?, ?, ?, NOW(), NOW()) RETURNING id
        """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, function.getOwnerId());
            ps.setLong(2, function.getFunctionTypeId());
            ps.setBytes(3, function.getSerializedData());
            ps.setString(4, function.getName());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Long id = rs.getLong(1);
                    logger.debug("Функция сохранена с id={}", id);
                    return id;
                } else {
                    throw new RuntimeException("Не удалось получить ID сохраненной функции");
                }
            }
        } catch (SQLException e) {
            logger.error("[SAVE] Ошибка сохранения функции '{}'", function.getName(), e);
            throw new RuntimeException("Сохранение функции не удалось", e);
        }
    }

    // === 1. ОДИНОЧНЫЙ ПОИСК ===
    @Override
    public Optional<TabulatedFunctionDTO> findByIdAndOwnerId(Long id, Long ownerId) {
        logger.debug("[SINGLE] Поиск функции id={} у пользователя {}", id, ownerId);
        String sql = "SELECT * FROM tabulated_functions WHERE id = ? AND owner_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.setLong(2, ownerId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    TabulatedFunctionDTO dto = map(rs);
                    logger.trace("Найдена функция: id={}, имя='{}', владелец={}", dto.getId(), dto.getName(), ownerId);
                    return Optional.of(dto);
                } else {
                    logger.debug("Функция id={} не найдена у пользователя {}", id, ownerId);
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            logger.error("[SINGLE] Ошибка поиска: id={}, owner={}", id, ownerId, e);
            throw new RuntimeException("Поиск функции не удался", e);
        }
    }

    // === 2. МНОЖЕСТВЕННЫЙ ПОИСК ===
    @Override
    public List<TabulatedFunctionDTO> findByOwnerId(Long ownerId) {
        logger.debug("[MULTI] Загрузка ВСЕХ функций пользователя {}", ownerId);
        String sql = "SELECT * FROM tabulated_functions WHERE owner_id = ? ORDER BY created_at DESC";
        return queryList(sql, ownerId);
    }

    // === 3. СОРТИРОВКА ===
    @Override
    public List<TabulatedFunctionDTO> findByOwnerIdSortedByNameAsc(Long ownerId) {
        logger.debug("[SORT] Функции пользователя {} → сортировка по имени (A→Я)", ownerId);
        String sql = "SELECT * FROM tabulated_functions WHERE owner_id = ? ORDER BY name ASC";
        return queryList(sql, ownerId);
    }

    @Override
    public List<TabulatedFunctionDTO> findByOwnerIdSortedByCreatedAtDesc(Long ownerId) {
        logger.debug("[SORT] Функции пользователя {} → сортировка по дате создания (новые→старые)", ownerId);
        String sql = "SELECT * FROM tabulated_functions WHERE owner_id = ? ORDER BY created_at DESC";
        return queryList(sql, ownerId);
    }

    // === 4. ФИЛЬТРАЦИЯ + СОРТИРОВКА ===
    @Override
    public List<TabulatedFunctionDTO> findByOwnerIdAndTypeId(Long ownerId, Long typeId) {
        logger.debug("[FILTER] Пользователь={}, тип={} → сортировка по дате", ownerId, typeId);
        String sql = """
            SELECT * FROM tabulated_functions 
            WHERE owner_id = ? AND function_type_id = ? 
            ORDER BY created_at DESC
            """;
        return queryList(sql, ownerId, typeId);
    }

    @Override
    public List<TabulatedFunctionDTO> findByOwnerIdAndTypeIdSortedByNameAsc(Long ownerId, Long typeId) {
        logger.debug("[FILTER+SORT] Пользователь={}, тип={} → сортировка по имени", ownerId, typeId);
        String sql = """
            SELECT * FROM tabulated_functions 
            WHERE owner_id = ? AND function_type_id = ? 
            ORDER BY name ASC
            """;
        return queryList(sql, ownerId, typeId);
    }

    @Override
    public void updateName(Long id, Long ownerId, String newName) {

    }

    @Override
    public void updateDataAndName(Long id, Long ownerId, TabulatedFunctionDTO newData) {

    }

    @Override
    public void deleteByIdAndOwnerId(Long id, Long ownerId) {

    }

    // === ВСПОМОГАТЕЛЬНЫЙ МЕТОД ===
    private List<TabulatedFunctionDTO> queryList(String sql, Object... params) {
        List<TabulatedFunctionDTO> list = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Ошибка выполнения запроса: {}", sql.substring(0, Math.min(50, sql.length())) + "...", e);
            throw new RuntimeException("Запрос не удался", e);
        }
        logger.info("Загружено {} функций", list.size());
        return list;
    }

    // === МАППИНГ ResultSet → DTO ===
    private TabulatedFunctionDTO map(ResultSet rs) throws SQLException {
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

    //вспомогательный метод для тестов
    public TabulatedFunctionDTO saveAndReturn(TabulatedFunctionDTO dto) {
        Long id = save(dto);
        return findByIdAndOwnerId(id, dto.getOwnerId()).orElseThrow();
    }
}