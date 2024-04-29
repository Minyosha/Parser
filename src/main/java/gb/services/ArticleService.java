package gb.services;

import gb.data.Article;
import gb.data.Project;
import gb.repository.ArticleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ArticleService {
    private final ArticleRepository articleRepository;

    public ArticleService(ArticleRepository articleRepository) {
        this.articleRepository = articleRepository;
    }

    public ArticleRepository getArticleRepository() {
        return articleRepository;
    }

    public List<Article> findByProject(Project selectedProject) {
        return articleRepository.findByProject(selectedProject);
    }

    public void deleteByProjectId(Long id) {
        articleRepository.deleteByProjectId(id);
    }

    public void save(Article article) {
        articleRepository.save(article);
    }

    // Дополнительные методы обработки бизнес-логики для Article
}