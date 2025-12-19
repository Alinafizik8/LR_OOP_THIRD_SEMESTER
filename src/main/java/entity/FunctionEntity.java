package entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "functions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FunctionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String functionType; // "ARRAY" or "LINKED_LIST"

    // Сохранение НЕ поточечно - массивы как JSON или сериализованные строки
    @Column(columnDefinition = "TEXT")
    private String xValues; // JSON array: "[1.0, 2.0, 3.0]"

    @Column(columnDefinition = "TEXT")
    private String yValues; // JSON array: "[1.0, 4.0, 9.0]"

    @Column
    private Integer count;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}
