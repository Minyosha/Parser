package gb.data;

import jakarta.persistence.*;

@Entity
@Table(name = "article")
public class Article {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String article_content;

    @ManyToOne
    private Project project;

    public Article() {
        // Default constructor
    }

    public Article(Project project, String line) {
        this.project = project;
        this.article_content = line;
    }

    public Article(Long id, String article_content, Project project) {
        this.id = id;
        this.article_content = article_content;
        this.project = project;
    }

    public Article(String article_content, Project project) {
        this.article_content = article_content;
        this.project = project;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getArticle_content() {
        return article_content;
    }

    public void setArticle_content(String article_content) {
        this.article_content = article_content;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }
}
