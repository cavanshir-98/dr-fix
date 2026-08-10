package tech.masterfix.model;

import jakarta.persistence.*;

@Entity
@Table(name = "appliance_services")
public class ApplianceService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String icon;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private String category;

    public ApplianceService() {}

    public ApplianceService(String name, String icon, String description, String category) {
        this.name = name;
        this.icon = icon;
        this.description = description;
        this.category = category;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}
