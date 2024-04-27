package gb.repository;

import gb.data.Projects;
import gb.data.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface ProjectsRepository extends JpaRepository<Projects, Long> {
    List<Projects> findAllByUser(User user);

    Page<Projects> findAllByUser(User user, Pageable pageable);

    List<Projects> findAllByUser_Id(Long id, Pageable pageable);


}