//// package dto.function;
//package dto.function;
//
//import java.time.Instant;
//import java.util.Objects;
//
//public class TabulatedFunctionDto {
//
//    private Long id;
//    private String name;
//    private String type;
//    private String localizedTypeName;
//    private Double xMin;
//    private Double xMax;
//    private Integer pointCount;
//    private String interpolationMethod;
//    private Instant createdAt;
//    private Instant updatedAt;
//
//    // Конструктор по умолчанию (обязателен для Jackson)
//    public TabulatedFunctionDto() {}
//
//    // Геттеры
//    public Long getId() { return id; }
//    public String getName() { return name; }
//    public String getType() { return type; }
//    public String getLocalizedTypeName() { return localizedTypeName; }
//    public Double getXMin() { return xMin; }
//    public Double getXMax() { return xMax; }
//    public Integer getPointCount() { return pointCount; }
//    public String getInterpolationMethod() { return interpolationMethod; }
//    public Instant getCreatedAt() { return createdAt; }
//    public Instant getUpdatedAt() { return updatedAt; }
//
//    // Сеттеры
//    public void setId(Long id) { this.id = id; }
//    public void setName(String name) { this.name = name; }
//    public void setType(String type) { this.type = type; }
//    public void setLocalizedTypeName(String localizedTypeName) { this.localizedTypeName = localizedTypeName; }
//    public void setXMin(Double xMin) { this.xMin = xMin; }
//    public void setXMax(Double xMax) { this.xMax = xMax; }
//    public void setPointCount(Integer pointCount) { this.pointCount = pointCount; }
//    public void setInterpolationMethod(String interpolationMethod) { this.interpolationMethod = interpolationMethod; }
//    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
//    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
//
//    // equals/hashCode/toString — для логов и тестов
//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//        TabulatedFunctionDto that = (TabulatedFunctionDto) o;
//        return Objects.equals(id, that.id);
//    }
//
//    @Override
//    public int hashCode() {
//        return Objects.hash(id);
//    }
//
//    @Override
//    public String toString() {
//        return "TabulatedFunctionDto{" +
//                "id=" + id +
//                ", name='" + name + '\'' +
//                ", type='" + type + '\'' +
//                ", localizedTypeName='" + localizedTypeName + '\'' +
//                ", xMin=" + xMin +
//                ", xMax=" + xMax +
//                ", pointCount=" + pointCount +
//                ", interpolationMethod='" + interpolationMethod + '\'' +
//                ", createdAt=" + createdAt +
//                ", updatedAt=" + updatedAt +
//                '}';
//    }
//}
// package dto.function;
package dto.function;

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