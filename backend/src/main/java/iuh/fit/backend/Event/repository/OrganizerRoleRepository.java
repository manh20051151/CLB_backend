package iuh.fit.backend.Event.repository;

import iuh.fit.backend.Event.Entity.OrganizerRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrganizerRoleRepository extends JpaRepository<OrganizerRole, String> {
    Optional<OrganizerRole> findByName(String name);
}
