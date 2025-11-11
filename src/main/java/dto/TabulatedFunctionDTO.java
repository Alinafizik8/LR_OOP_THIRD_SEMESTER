package dto;

import functions.TabulatedFunction;
import io.FunctionsIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * DTO для таблицы tabulated_functions.
 * Хранит сериализованные данные как byte[], без десериализации в объект.
 * Десериализация производится ТОЛЬКО в сервисном слое (например, при отрисовке графика).
 */
public class TabulatedFunctionDTO {
    private static final Logger logger = LoggerFactory.getLogger(TabulatedFunctionDTO.class);

    private Long id;
    private Long ownerId;
    private Long functionTypeId;
    private byte[] serializedData; // ← только байты, не объект
    private String name;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // === Конструктор для сохранения новой функции ===
    // Принимает уже сериализованные данные (например, из UI или из FunctionsIO.serialize)
    public TabulatedFunctionDTO(Long id, Long ownerId, Long functionTypeId, byte[] serializedData, String name, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.ownerId = ownerId;
        this.functionTypeId = functionTypeId;
        this.serializedData = serializedData != null ? serializedData.clone() : null;
        this.name = name;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // === Конструктор без времени (для тестов/сервлетов) ===
    public TabulatedFunctionDTO(Long ownerId, Long functionTypeId, byte[] serializedData, String name) {
        this(null, ownerId, functionTypeId, serializedData, name, null, null);
    }

    // === Конструктор по умолчанию (для Jackson/Gson/маппинга) ===
    public TabulatedFunctionDTO() {}

    // === Фабричный метод: создание DTO из ResultSet ===
    public static TabulatedFunctionDTO fromResultSet(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id");
        Long ownerId = rs.getLong("owner_id");
        Long functionTypeId = rs.getLong("function_type_id");
        String name = rs.getString("name");
        byte[] serializedData = rs.getBytes("serialized_data"); // Может быть null
        LocalDateTime createdAt = rs.getObject("created_at", LocalDateTime.class);
        LocalDateTime updatedAt = rs.getObject("updated_at", LocalDateTime.class);

        logger.debug("Loaded DTO from DB: id={}, name='{}', dataLength={}",
                id, name, serializedData != null ? serializedData.length : 0);

        return new TabulatedFunctionDTO(id, ownerId, functionTypeId, serializedData, name, createdAt, updatedAt);
    }

    // === Геттеры и сеттеры ===

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }

    public Long getFunctionTypeId() { return functionTypeId; }
    public void setFunctionTypeId(Long functionTypeId) { this.functionTypeId = functionTypeId; }

    public byte[] getSerializedData() {
        return serializedData != null ? serializedData.clone() : null;
    }
    public void setSerializedData(byte[] serializedData) {
        this.serializedData = serializedData != null ? serializedData.clone() : null;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // === Утилиты — вынесены как статические методы (для использования в сервисе, НЕ в DTO) ===

    /**
     * Сериализует функцию в byte[].
     * Использовать в сервисе/контроллере перед сохранением в БД.
     */
    public static byte[] serializeFunction(TabulatedFunction function) {
        if (function == null) {
            logger.warn("Attempt to serialize null function → returning null");
            return null;
        }
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             BufferedOutputStream bos = new BufferedOutputStream(baos)) {
            FunctionsIO.serialize(bos, function);
            bos.flush();
            byte[] data = baos.toByteArray();
            logger.debug("Function serialized: {} points, size={}B", function.getCount(), data.length);
            return data;
        } catch (IOException e) {
            logger.error("Failed to serialize function", e);
            throw new RuntimeException("Serialization failed", e);
        }
    }

    /**
     * Десериализует byte[] в функцию.
     * Использовать в сервисе/контроллере после загрузки из БД.
     */
    public static TabulatedFunction deserializeFunction(byte[] data) {
        if (data == null || data.length == 0) {
            logger.warn("Cannot deserialize null or empty data");
            return null;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data))) {
            TabulatedFunction func = (TabulatedFunction) ois.readObject();
            logger.debug("Function deserialized: {} points", func.getCount());
            return func;
        } catch (IOException | ClassNotFoundException e) {
            logger.error("Failed to deserialize function from {}B", data.length, e);
            throw new RuntimeException("Deserialization failed", e);
        }
    }

    // === toString, equals, hashCode ===

    @Override
    public String toString() {
        return "TabulatedFunctionDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", ownerId=" + ownerId +
                ", functionTypeId=" + functionTypeId +
                ", dataLength=" + (serializedData != null ? serializedData.length : 0) +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TabulatedFunctionDTO that = (TabulatedFunctionDTO) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(ownerId, that.ownerId) &&
                Objects.equals(functionTypeId, that.functionTypeId) &&
                Objects.equals(name, that.name) &&
                Objects.equals(createdAt, that.createdAt) &&
                Objects.equals(updatedAt, that.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, ownerId, functionTypeId, name, createdAt, updatedAt);
    }
}
