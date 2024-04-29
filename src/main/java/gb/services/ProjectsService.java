package gb.services;

import gb.data.Article;
import gb.data.Project;
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

    public List<Project> getAllProjects() {
        return projectsRepository.findAllByUser(authenticatedUser.get().get());
    }

    public Optional<Project> getProjectById(Long id) {
        return projectsRepository.findById(id);
    }

    public Project createProject(Project project) {
        project.setUser(authenticatedUser.get().get());
        return projectsRepository.save(project);
    }

    public Project updateProject(Long id, Project project) {
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

    public Page<Project> list(Pageable pageable) {
        return projectsRepository.findAllByUser(authenticatedUser.get().get(), pageable);
    }


    public Page<Project> listByUser(Pageable pageable) {
        return projectsRepository.findAllByUser(authenticatedUser.get().get(), pageable);
    }


    public Project findById(Long id) {
        return projectsRepository.findById(id).orElse(null);
    }

    public void update(Project projectToUpdate) {
        projectsRepository.save(projectToUpdate);
    }

    public List<Project> findAllByUserId(Long userId, PageRequest pageRequest) {
        return projectsRepository.findAllByUser_Id(userId, pageRequest);
    }

    public List<Article> getArticlesByProject(Long projectId) {
        Project selectedProject = projectsRepository.findById(projectId)
                .orElseThrow(() -> new NoSuchElementException("Project not found"));

        return articleRepository.findByProject(selectedProject);
    }

    public List<Variants> getVariantsByProject(Long projectId) {
        Project selectedProject = projectsRepository.findById(projectId)
                .orElseThrow(() -> new NoSuchElementException("Project not found"));

        return variantsRepository.findByProject(selectedProject);
    }

    public void saveJsonData(Project selectedProject) {
        projectsRepository.save(selectedProject);
    }

}