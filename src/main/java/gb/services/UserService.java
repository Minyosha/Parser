package gb.services;

import gb.data.Role;
import gb.data.User;
import gb.data.UserRepository;

import java.util.*;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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

// не работает
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


// старый с глюком
//    public Page<User> filteredList(String value, String key, PageRequest of) {
//        Specification<User> spec = (root, query, cb) -> {
//            List<Predicate> predicates = new ArrayList<>();
//
//            if (value == null || value.isEmpty()) {
//                return cb.isTrue(cb.literal(true)); // always true = no filtering
//            }
//
//            if ("id".equals(key)) {
//                predicates.add(cb.equal(root.get(key), value));
//            } else if ("banned".equals(key)) {
//                predicates.add(cb.equal(root.get(key), Boolean.parseBoolean(value)));
//            } else if ("roles".equals(key)) {
//                Join<User, Role> roleJoin = root.join("roles");
//                CriteriaBuilder.In<Role> in = cb.in(roleJoin);
//                for (String roleValue : value.split(",")) {
//                    in = in.value(Role.valueOf(roleValue));
//                }
//                predicates.add(in);
//            } else if (value.matches("^[a-zA-Z0-9]+$")) {
//                predicates.add(cb.like(root.get(key), "%" + value + "%"));
//            } else {
//                predicates.add(cb.isTrue(cb.literal(true))); // default to no filtering
//            }
//
//            return cb.and(predicates.toArray(new Predicate[0]));
//        };
//
//        return repository.findAll(spec, of);
//    }


    public Page<User> filteredList(String filterValue, String columnKey, Pageable pageable) {
        System.out.println("filterValue: " + filterValue);
        System.out.println("columnKey: " + columnKey);
        System.out.println("pageable: " + pageable);
        Specification<User> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filterValue != null && !filterValue.isEmpty()) {
                if ("id".equals(columnKey)) {
                    predicates.add(cb.equal(root.get(columnKey), filterValue));
                } else if ("banned".equals(columnKey)) {
                    predicates.add(cb.equal(root.get(columnKey), Boolean.parseBoolean(filterValue)));
                } else if ("roles".equals(columnKey)) {
                    Join<User, Role> roleJoin = root.join("roles");
                    CriteriaBuilder.In<Role> in = cb.in(roleJoin);
                    for (String roleValue : filterValue.split(",")) {
                        in = in.value(Role.valueOf(roleValue));
                    }
                    predicates.add(in);
                } else if (filterValue.matches("^[a-zA-Z0-9]+$")) {
                    predicates.add(cb.like(root.get(columnKey), "%" + filterValue + "%"));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return repository.findAll(spec, pageable);
    }


    public Page<User> filteredList(List<String> filterValues, Pageable pageable) {
        Specification<User> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filterValues.size() >= 6) {
                if (filterValues.get(0) != null && !filterValues.get(0).isEmpty()) {
                    predicates.add(cb.equal(root.get("id"), filterValues.get(0)));
                }
                if (filterValues.get(1) != null && !filterValues.get(1).isEmpty()) {
                    predicates.add(cb.equal(root.get("banned"), Boolean.parseBoolean(filterValues.get(1))));
                }
                if (filterValues.get(2) != null && !filterValues.get(2).isEmpty()) {
                    predicates.add(cb.like(root.get("username"), "%" + filterValues.get(2) + "%"));
                }
                if (filterValues.get(3) != null && !filterValues.get(3).isEmpty()) {
                    predicates.add(cb.like(root.get("name"), "%" + filterValues.get(3) + "%"));
                }
                if (filterValues.get(4) != null && !filterValues.get(4).isEmpty()) {
                    Join<User, Role> roleJoin = root.join("roles");
                    CriteriaBuilder.In<Role> in = cb.in(roleJoin);
                    for (String roleValue : filterValues.get(4).split(",")) {
                        in = in.value(Role.valueOf(roleValue));
                    }
                    predicates.add(in);
                }
                if (filterValues.get(5) != null && !filterValues.get(5).isEmpty()) {
                    predicates.add(cb.like(root.get("email"), "%" + filterValues.get(5) + "%"));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return repository.findAll(spec, pageable);
    }














}
