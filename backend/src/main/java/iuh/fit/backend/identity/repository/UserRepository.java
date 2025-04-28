package iuh.fit.backend.identity.repository;

import iuh.fit.backend.identity.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    boolean existsByUsername(String username);
    Optional<User> findByUsername(String username);

    // Tìm các User có cả position và organizerRole khác null
    @Query("SELECT u FROM User u WHERE u.position IS NOT NULL AND u.organizerRole IS NOT NULL")
    List<User> findUsersWithPositionAndOrganizerRole();
}
