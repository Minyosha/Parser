package gb.repository;

import gb.data.Project;
import gb.data.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface ProjectsRepository extends JpaRepository<Project, Long> {
    List<Project> findAllByUser(User user);

    Page<Project> findAllByUser(User user, Pageable pageable);

    List<Project> findAllByUser_Id(Long id, Pageable pageable);


}