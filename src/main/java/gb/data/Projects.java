package gb.data;

import jakarta.persistence.*;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.domain.PageRequest;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

@Entity
@Table(name = "projects")
@DynamicUpdate
public class Projects {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private int version;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> articles = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<String> getArticles() {
        return articles;
    }

    public void setArticles(Set<String> articles) {
        this.articles = articles;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

}
