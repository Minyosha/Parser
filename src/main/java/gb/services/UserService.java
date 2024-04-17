package gb.services;

import gb.data.Role;
import gb.data.User;
import gb.data.UserRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class UserService {

    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public Optional<User> get(Long id) {
        return repository.findById(id);
    }

    public User update(User entity) {
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public Page<User> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<User> list(Pageable pageable, Specification<User> filter) {
        return repository.findAll(filter, pageable);
    }

    public int count() {
        return (int) repository.count();
    }

    public User findById(Long id) {
        return repository.findById(id).orElse(null);
    }

//    public Page<User> filteredList(String value, String key, PageRequest of) {
//        Specification<User> spec = (root, query, cb) -> {
//            if (value == null || value.isEmpty()) {
//                return cb.isTrue(cb.literal(true)); // always true = no filtering
//            }
//            if ("id".equals(key)) {
//                return cb.equal(root.get(key), value);
//            } else {
//                return cb.like(root.get(key), "%" + value + "%");
//            }
//        };
//        return repository.findAll(spec, of);
//    }


//    public Page<User> filteredList(String value, String key, PageRequest of) {
//        Specification<User> spec = (root, query, cb) -> {
//            if (value == null || value.isEmpty()) {
//                return cb.isTrue(cb.literal(true)); // always true = no filtering
//            }
//            if ("id".equals(key)) {
//                return cb.equal(root.get(key), value);
//            } else if ("banned".equals(key)) {
//                return cb.equal(root.get(key), Boolean.parseBoolean(value));
//            } else if ("roles".equals(key)) {
//                Join<User, Role> roleJoin = root.join("roles");
//                CriteriaBuilder.In<Role> in = cb.in(roleJoin);
//                for (String roleValue : value.split(",")) {
//                    in = in.value(Role.valueOf(roleValue));
//                }
//                return in;
//            } else if (value.matches("^[a-zA-Z0-9]+$")) {
//                return cb.like(root.get(key), "%" + value + "%");
//            } else {
//                return cb.isTrue(cb.literal(true)); // default to no filtering
//            }
//        };
//        return repository.findAll(spec, of);
//    }



    public Page<User> filteredList(String value, String key, PageRequest of) {
        Specification<User> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (value == null || value.isEmpty()) {
                return cb.isTrue(cb.literal(true)); // always true = no filtering
            }

            if ("id".equals(key)) {
                predicates.add(cb.equal(root.get(key), value));
            } else if ("banned".equals(key)) {
                predicates.add(cb.equal(root.get(key), Boolean.parseBoolean(value)));
            } else if ("roles".equals(key)) {
                Join<User, Role> roleJoin = root.join("roles");
                CriteriaBuilder.In<Role> in = cb.in(roleJoin);
                for (String roleValue : value.split(",")) {
                    in = in.value(Role.valueOf(roleValue));
                }
                predicates.add(in);
            } else if (value.matches("^[a-zA-Z0-9]+$")) {
                predicates.add(cb.like(root.get(key), "%" + value + "%"));
            } else {
                predicates.add(cb.isTrue(cb.literal(true))); // default to no filtering
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return repository.findAll(spec, of);
    }
}
