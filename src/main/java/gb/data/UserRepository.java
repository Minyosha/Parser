package gb.data;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import javax.annotation.Nullable;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    @Nullable
    User findByUsername(String username);
    
    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}