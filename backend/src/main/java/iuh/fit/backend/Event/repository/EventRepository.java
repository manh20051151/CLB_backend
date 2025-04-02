package iuh.fit.backend.Event.repository;

import iuh.fit.backend.Event.Entity.Event;
import iuh.fit.backend.Event.enums.EventStatus;
import iuh.fit.backend.identity.entity.Permission;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface EventRepository extends JpaRepository<Event, String> {
    List<Event> findByStatus(EventStatus status);
}
