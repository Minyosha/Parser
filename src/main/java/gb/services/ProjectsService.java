package gb.services;

import gb.data.Article;
import gb.data.Projects;
import gb.data.Variants;
import gb.repository.ArticleRepository;
import gb.repository.ProjectsRepository;
import gb.repository.VariantsRepository;
import gb.security.AuthenticatedUser;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@Transactional
public class ProjectsService {

    private final ProjectsRepository projectsRepository;
    private final AuthenticatedUser authenticatedUser;
    private final ArticleRepository articleRepository;
    private final VariantsRepository variantsRepository;

    @Autowired
    public ProjectsService(ProjectsRepository projectsRepository, AuthenticatedUser authenticatedUser, ArticleRepository articleRepository, VariantsRepository variantsRepository) {
        this.projectsRepository = projectsRepository;
        this.authenticatedUser = authenticatedUser;
        this.articleRepository = articleRepository;
        this.variantsRepository = variantsRepository;
    }

    public List<Projects> getAllProjects() {
        return projectsRepository.findAllByUser(authenticatedUser.get().get());
    }

    public Optional<Projects> getProjectById(Long id) {
        return projectsRepository.findById(id);
    }

    public Projects createProject(Projects project) {
        project.setUser(authenticatedUser.get().get());
        return projectsRepository.save(project);
    }

    public Projects updateProject(Long id, Projects project) {
        return projectsRepository.findById(id)
                .map(existingProject -> {
                    existingProject.setTitle(project.getTitle());
                    existingProject.setDescription(project.getDescription());
                    existingProject.setUser(project.getUser());
                    return projectsRepository.save(existingProject);
                })
                .orElseThrow(() -> new EntityNotFoundException("Project not found with ID: " + id));
    }

    public void deleteProject(Long id) {
        projectsRepository.deleteById(id);
    }

    public Page<Projects> list(Pageable pageable) {
        return projectsRepository.findAllByUser(authenticatedUser.get().get(), pageable);
    }


    public Page<Projects> listByUser(Pageable pageable) {
        return projectsRepository.findAllByUser(authenticatedUser.get().get(), pageable);
    }


    public Projects findById(Long id) {
        return projectsRepository.findById(id).orElse(null);
    }

    public void update(Projects projectToUpdate) {
        projectsRepository.save(projectToUpdate);
    }

    public List<Projects> findAllByUserId(Long userId, PageRequest pageRequest) {
        return projectsRepository.findAllByUser_Id(userId, pageRequest);
    }

    public List<Article> getArticlesByProject(Long projectId) {
        Projects selectedProject = projectsRepository.findById(projectId)
                .orElseThrow(() -> new NoSuchElementException("Project not found"));

        return articleRepository.findByProject(selectedProject);
    }

    public List<Variants> getVariantsByProject(Long projectId) {
        Projects selectedProject = projectsRepository.findById(projectId)
                .orElseThrow(() -> new NoSuchElementException("Project not found"));

        return variantsRepository.findByProject(selectedProject);
    }
}