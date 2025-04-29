package iuh.fit.backend.identity.repository;

import iuh.fit.backend.identity.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    List<User> findByRoles_Name(String roleName);

    // Tìm user đã bị khóa bằng native query
    @Query(value = "SELECT * FROM user WHERE id = ?1 AND locked = true", nativeQuery = true)
    Optional<User> findLockedUserById(String userId);

    // Lấy danh sách user bị khóa
    @Query(value = "SELECT * FROM user WHERE locked = true", nativeQuery = true)
    Page<User> findLockedUsers(Pageable pageable);
}
