package gb.services;

import gb.data.SamplePerson;
import gb.data.SamplePersonRepository;

import java.util.Collection;
import java.util.Optional;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class SamplePersonService {

    private final SamplePersonRepository repository;

    public SamplePersonService(SamplePersonRepository repository) {
        this.repository = repository;
    }

    public Optional<SamplePerson> get(Long id) {
        return repository.findById(id);
    }

    public SamplePerson update(SamplePerson entity) {
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public Page<SamplePerson> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<SamplePerson> list(Pageable pageable, Specification<SamplePerson> filter) {
        return repository.findAll(filter, pageable);
    }

    public int count() {
        return (int) repository.count();
    }

    public Page<SamplePerson> findByFirstNameAndEmail(String firstName, String email, Pageable pageable) {
        // Здесь должна быть логика для поиска SamplePerson по имени и email
        // Это может быть запрос к базе данных, который фильтрует записи по этим параметрам
        return repository.findByFirstNameContainingAndEmailContaining(firstName, email, pageable);
    }
}
