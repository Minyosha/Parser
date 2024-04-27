package gb.services;

import gb.data.Projects;
import gb.data.Variants;
import gb.repository.VariantsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class VariantsService {
    private final VariantsRepository variantsRepository;

    public VariantsService(VariantsRepository variantsRepository) {
        this.variantsRepository = variantsRepository;
    }

    public VariantsRepository getVariantsRepository() {
        return variantsRepository;
    }

    public List<Variants> findByProject(Projects selectedProject) {
        return variantsRepository.findByProject(selectedProject);
    }

    public void deleteByProjectId(Long id) {
        variantsRepository.deleteByProjectId(id);
    }
}
