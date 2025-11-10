package dto;

import functions.TabulatedFunction;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

public class FunctionFullDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 8390414562223467886L;
    private Long id;
    private String name;
    private String typeName; // например, "TabulatedFunction", "SqrFunction"
    private double[] xValues;
    private double[] yValues;
    private LocalDateTime createdAt;

    // конструкторы
    public FunctionFullDto() {}

    public FunctionFullDto(Long id, String name, String typeName, double[] x, double[] y, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.typeName = typeName;
        this.xValues = x != null ? x.clone() : null;
        this.yValues = y != null ? y.clone() : null;
        this.createdAt = createdAt;
    }

    public static FunctionFullDto fromTabulatedFunction(TabulatedFunction f, Long id, String name, String typeName, LocalDateTime createdAt) {
        int n = f.getCount();
        double[] xs = new double[n];
        double[] ys = new double[n];
        for (int i = 0; i < n; i++) {
            xs[i] = f.getX(i);
            ys[i] = f.getY(i);
        }
        return new FunctionFullDto(id, name, typeName, xs, ys, createdAt);
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getTypeName() { return typeName; }
    public double[] getXValues() { return xValues != null ? xValues.clone() : null; }
    public double[] getYValues() { return yValues != null ? yValues.clone() : null; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}