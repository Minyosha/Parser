package gb.data;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "articles")
public class Article {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String article_content;

    @ManyToOne
    private Projects project;

    public Article() {
        // Default constructor
    }

    public Article(Projects project, String line) {
        this.project = project;
        this.article_content = line;
    }

    public Article(Long id, String article_content, Projects project) {
        this.id = id;
        this.article_content = article_content;
        this.project = project;
    }

    public Article(String article_content, Projects project) {
        this.article_content = article_content;
        this.project = project;
    }
// геттеры, сеттеры и т.д.

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

    public Projects getProject() {
        return project;
    }

    public void setProject(Projects project) {
        this.project = project;
    }
}
