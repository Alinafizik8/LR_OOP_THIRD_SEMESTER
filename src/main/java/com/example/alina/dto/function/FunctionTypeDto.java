package com.example.alina.dto.function;

import java.time.Instant;
import java.util.Objects;

public class FunctionTypeDto {

    private Long id;
    private String name;
    private String localizedName;
    private Integer priority;
    private Instant createdAt;
    private Instant updatedAt;

    // Конструктор по умолчанию (обязателен для Jackson при десериализации из JSON)
    public FunctionTypeDto() {}

    // Конструктор для удобства (опционален)
    public FunctionTypeDto(Long id, String name, String localizedName, Integer priority, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.localizedName = localizedName;
        this.priority = priority;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Геттеры
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLocalizedName() {
        return localizedName;
    }

    public Integer getPriority() {
        return priority;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    // Сеттеры
    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLocalizedName(String localizedName) {
        this.localizedName = localizedName;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    // equals / hashCode — важно для коллекций и тестов
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FunctionTypeDto that = (FunctionTypeDto) o;
        return Objects.equals(id, that.id)
                && Objects.equals(name, that.name)
                && Objects.equals(localizedName, that.localizedName)
                && Objects.equals(priority, that.priority)
                && Objects.equals(createdAt, that.createdAt)
                && Objects.equals(updatedAt, that.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, localizedName, priority, createdAt, updatedAt);
    }

    // toString — полезен для логов и отладки
    @Override
    public String toString() {
        return "FunctionTypeDto{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", localizedName='" + localizedName + '\'' +
                ", priority=" + priority +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}