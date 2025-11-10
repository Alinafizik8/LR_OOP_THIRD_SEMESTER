package functions.dto;

import java.time.LocalDateTime;

/**
 * DTO для таблицы tabulated_functions.
 * Используется для передачи данных между DAO и сервлетами (лаба №6).
 */
public class TabulatedFunctionDTO {
    private Long id;
    private Long ownerId;
    private Long functionTypeId;
    private byte[] serializedData;
    private String name;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Конструктор по умолчанию (для Jackson/Gson)
    public TabulatedFunctionDTO() {}

    public TabulatedFunctionDTO(Long ownerId, Long functionTypeId, byte[] serializedData, String name) {
        this.ownerId = ownerId;
        this.functionTypeId = functionTypeId;
        this.serializedData = serializedData;
        this.name = name;
    }

    // Геттеры и сеттеры (обязательны для сериализации)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }

    public Long getFunctionTypeId() { return functionTypeId; }
    public void setFunctionTypeId(Long functionTypeId) { this.functionTypeId = functionTypeId; }

    public byte[] getSerializedData() { return serializedData; }
    public void setSerializedData(byte[] serializedData) { this.serializedData = serializedData; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "TabulatedFunctionDTO{id=" + id + ", name='" + name + "', ownerId=" + ownerId + "}";
    }
}