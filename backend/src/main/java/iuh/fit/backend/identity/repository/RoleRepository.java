package iuh.fit.backend.identity.repository;

import iuh.fit.backend.identity.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, String> {

}
