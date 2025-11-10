package dto;

import functions.LinkedListTabulatedFunction;
import functions.TabulatedFunction;
import io.FunctionsIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Objects;

public class TabulatedFunctionDTO {
    private static final Logger logger = LoggerFactory.getLogger(TabulatedFunctionDTO.class);

    private Long id;
    private Long ownerId;
    private Long functionTypeId;
    private byte[] serializedData;
    private String name;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private TabulatedFunction function; // Поле для десериализованной функции

    // Конструктор для создания DTO при сохранении новой функции в БД
    // функция передаётся как объект, затем сериализуется
    public TabulatedFunctionDTO(Long id, Long ownerId, Long functionTypeId, TabulatedFunction function, String name, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.ownerId = ownerId;
        this.functionTypeId = functionTypeId;
        this.function = function;
        this.serializedData = serializeFunction(function);
        this.name = name;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Конструктор для создания DTO из ResultSet (при загрузке из БД)
    // байты загружаются из БД, затем десериализуются в объект
    private TabulatedFunctionDTO(Long id, Long ownerId, Long functionTypeId, byte[] serializedData, String name, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.ownerId = ownerId;
        this.functionTypeId = functionTypeId;
        this.serializedData = serializedData != null ? serializedData.clone() : null;
        this.name = name;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    //метод для создания DTO из ResultSet
    public static TabulatedFunctionDTO fromResultSet(ResultSet rs) throws SQLException {
        try {
            Long id = rs.getLong("id");
            Long ownerId = rs.getLong("owner_id");
            Long functionTypeId = rs.getLong("function_type_id");
            String name = rs.getString("name");
            byte[] serializedData = rs.getBytes("serialized_data"); // Может быть null
            LocalDateTime createdAt = rs.getObject("created_at", LocalDateTime.class);
            LocalDateTime updatedAt = rs.getObject("updated_at", LocalDateTime.class);

            TabulatedFunction function = null;
            if (serializedData != null) {
                logger.debug("Deserializing function data for ID: {}", id);
                function = FunctionsIO.deserialize(new java.io.BufferedInputStream(new ByteArrayInputStream(serializedData)));
                logger.debug("Function deserialized successfully for ID: {}", id);
            } else {
                logger.warn("Serialized data is null for function ID: {}. Function object will be null.", id);
            }

            TabulatedFunctionDTO dto = new TabulatedFunctionDTO(id, ownerId, functionTypeId, serializedData, name, createdAt, updatedAt);
            dto.function = function;

            return dto;
        } catch (IOException | ClassNotFoundException e) {
            logger.error("Error deserializing function data from ResultSet for a row", e);
            throw new RuntimeException("Failed to deserialize function from database data", e);
        }
    }

    public TabulatedFunction getFunction() {
        return function;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public Long getFunctionTypeId() {
        return functionTypeId;
    }

    public void setFunctionTypeId(Long functionTypeId) {
        this.functionTypeId = functionTypeId;
    }

    public byte[] getSerializedData() {
        return serializedData;
    }

    public void setSerializedData(byte[] serializedData) {
        this.serializedData = serializedData;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
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

    @Override
    public String toString() {
        return "TabulatedFunctionDTO{" +
                "id=" + id +
                ", ownerId=" + ownerId +
                ", functionTypeId=" + functionTypeId +
                ", serializedDataLength=" + (serializedData != null ? serializedData.length : 0) +
                ", name='" + name + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }

    public byte[] serializeFunction(TabulatedFunction function) {
        if (function == null) {
            logger.warn("Attempting to serialize a null function, returning null bytes.");
            return null;
        }
        try (java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
             java.io.BufferedOutputStream bos = new java.io.BufferedOutputStream(baos)) {
            FunctionsIO.serialize(bos, function);
            bos.flush(); // Ensure all data is written
            return baos.toByteArray();
        } catch (IOException e) {
            logger.error("Error serializing function for DTO creation", e);
            throw new RuntimeException("Failed to serialize function for database storage", e);
        }
    }

    public static TabulatedFunction deserializeFunction(byte[] bytes) throws IOException, ClassNotFoundException {
        if (bytes == null) return null;
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            return (TabulatedFunction) ois.readObject();
        }
    }
}
