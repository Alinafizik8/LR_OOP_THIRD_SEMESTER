package Entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "functions")
public class TabulatedFunctionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // FetchType.LAZY - загрузка по требованию
    @JoinColumn(name = "owner_id", nullable = false)
    private UserEntity owner; // Связь с UserEntity

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "function_type_id", nullable = false)
    private FunctionTypeEntity functionType; // Связь с FunctionTypeEntity

    @Lob // Large Object - для BLOB
    @Column(name = "serialized_data", nullable = false, columnDefinition = "BYTEA") // Указываем BYTEA для PostgreSQL
    private byte[] serializedData; // Хранение сериализованного объекта TabulatedFunction

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Конструктор по умолчанию
    protected TabulatedFunctionEntity() {}

    // Основной конструктор
    public TabulatedFunctionEntity(UserEntity owner, FunctionTypeEntity functionType, byte[] serializedData, String name) {
        this.owner = owner;
        this.functionType = functionType;
        this.serializedData = serializedData;
        this.name = name;
    }

    // Геттеры и сеттеры
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserEntity getOwner() {
        return owner;
    }

    public void setOwner(UserEntity owner) {
        this.owner = owner;
    }

    public FunctionTypeEntity getFunctionType() {
        return functionType;
    }

    public void setFunctionType(FunctionTypeEntity functionType) {
        this.functionType = functionType;
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

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}