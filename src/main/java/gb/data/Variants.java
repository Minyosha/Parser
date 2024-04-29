package gb.data;

import jakarta.persistence.*;

@Entity
@Table(name = "variant")
public class Variants {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String variant_content;

    @ManyToOne
    private Project project;

    public Variants() {
        // Default constructor
    }

    public Variants(Project project, String line) {
        this.project = project;
        this.variant_content = line;
    }

    public Variants(Long id, String variant_content, Project project) {
        this.id = id;
        this.variant_content = variant_content;
        this.project = project;
    }

    public Variants(String variant_content, Project project) {
        this.variant_content = variant_content;
        this.project = project;
    }

    // геттеры, сеттеры и т.д.

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVariant_content() {
        return variant_content;
    }

    public void setVariant_content(String variant_content) {
        this.variant_content = variant_content;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    @Override
    public String toString() {
        return "Variants{" +
                "id=" + id +
                ", variant_content='" + variant_content + '\'' +
                ", project=" + project +
                '}';
    }

}
