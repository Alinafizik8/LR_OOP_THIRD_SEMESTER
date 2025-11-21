package com.example.alina.dto.function;

import java.time.Instant;
import java.util.Objects;

public class TabulatedFunctionDto {

    private Long id;
    private String name;
    private String type; // техническое имя типа (например, "LINEAR")
    private String localizedTypeName; // локализованное название типа (например, "Линейная")

    private Instant createdAt;
    private Instant updatedAt;

    // Конструктор по умолчанию
    public TabulatedFunctionDto() {}

    // Геттеры
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getType() { return type; }
    public String getLocalizedTypeName() { return localizedTypeName; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    // Сеттеры
    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setType(String type) { this.type = type; }
    public void setLocalizedTypeName(String localizedTypeName) { this.localizedTypeName = localizedTypeName; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TabulatedFunctionDto that = (TabulatedFunctionDto) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "TabulatedFunctionDto{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", localizedTypeName='" + localizedTypeName + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}