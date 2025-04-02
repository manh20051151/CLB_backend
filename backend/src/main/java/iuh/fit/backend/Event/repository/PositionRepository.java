package iuh.fit.backend.Event.repository;


import iuh.fit.backend.Event.Entity.Position;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PositionRepository extends JpaRepository<Position, String> {
    Optional<Position> findByName(String name);
}
