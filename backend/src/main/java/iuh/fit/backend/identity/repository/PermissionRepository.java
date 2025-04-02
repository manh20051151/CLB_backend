package iuh.fit.backend.identity.repository;

import iuh.fit.backend.identity.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, String> {
    List<Permission> findByNameIn(Set<String> names);
}
