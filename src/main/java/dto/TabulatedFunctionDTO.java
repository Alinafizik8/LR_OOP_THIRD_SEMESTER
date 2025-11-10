package dto;

import java.time.LocalDateTime;
import java.util.Objects;

public class TabulatedFunctionDTO {
    private Long id;
    private Long ownerId;
    private Long functionTypeId;
    private byte[] serializedData;
    private String name;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public TabulatedFunctionDTO(Long id, Long ownerId, Long functionTypeId, byte[] serializedData, String name, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.ownerId = ownerId;
        this.functionTypeId = functionTypeId;
        this.serializedData = serializedData;
        this.name = name;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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
}
