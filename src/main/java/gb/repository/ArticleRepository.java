package gb.repository;

import gb.data.Article;
import gb.data.Projects;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ArticleRepository extends JpaRepository<Article, Long>{

    List<Article> findByProject(Projects selectedProject);

    @Modifying
    @Query("DELETE FROM Article a WHERE a.project.id = :projectId")
    void deleteByProjectId(@Param("projectId") Long projectId);
}
