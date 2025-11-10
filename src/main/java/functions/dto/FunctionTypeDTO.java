package functions.dto;

public class FunctionTypeDTO {
    private Long id;
    private String name;           // напр. "SIN"
    private String localizedName;  // напр. "Синус"
    private int priority;

    public FunctionTypeDTO() {}
    public FunctionTypeDTO(String name, String localizedName, int priority) {
        this.name = name;
        this.localizedName = localizedName;
        this.priority = priority;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLocalizedName() { return localizedName; }
    public void setLocalizedName(String localizedName) { this.localizedName = localizedName; }
    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }

    @Override
    public String toString() {
        return "FunctionTypeDTO{id=" + id + ", name='" + name + "', localizedName='" + localizedName + "'}";
    }
}