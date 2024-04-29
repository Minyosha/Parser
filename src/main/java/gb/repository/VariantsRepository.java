package gb.repository;

import gb.data.Project;
import gb.data.Variants;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VariantsRepository extends JpaRepository<Variants, Long> {
    List<Variants> findByProject(Project selectedProject);

    @Modifying
    @Query("DELETE FROM Variants a WHERE a.project.id = :projectId")
    void deleteByProjectId(@Param("projectId") Long projectId);

}
