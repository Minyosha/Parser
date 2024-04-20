package gb.services;

import gb.data.Projects;
import gb.data.ProjectsRepository;
import gb.security.AuthenticatedUser;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Service
@Transactional
public class ProjectsService {

    private final ProjectsRepository projectsRepository;
    private final AuthenticatedUser authenticatedUser;

    @Autowired
    public ProjectsService(ProjectsRepository projectsRepository, AuthenticatedUser authenticatedUser) {
        this.projectsRepository = projectsRepository;
        this.authenticatedUser = authenticatedUser;
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
}