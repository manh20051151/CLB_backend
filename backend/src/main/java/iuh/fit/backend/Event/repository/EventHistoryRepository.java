package iuh.fit.backend.Event.repository;

import iuh.fit.backend.Event.Entity.EventHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventHistoryRepository extends JpaRepository<EventHistory, String> {
    List<EventHistory> findByEventIdOrderByUpdatedAtDesc(String eventId);
}
