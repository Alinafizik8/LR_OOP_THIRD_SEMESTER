package functions.dto;

import java.time.LocalDateTime;

public class TabulatedFunctionDTO {
    private Long id;
    private Long ownerId;
    private Long functionTypeId;
    private byte[] serializedData;
    private String name;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Getters and Setters
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
}