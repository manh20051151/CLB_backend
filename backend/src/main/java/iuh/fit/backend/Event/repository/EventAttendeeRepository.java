package iuh.fit.backend.Event.repository;

import iuh.fit.backend.Event.Entity.Event;
import iuh.fit.backend.Event.Entity.EventAttendee;
import iuh.fit.backend.identity.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EventAttendeeRepository extends JpaRepository<EventAttendee, String> {
    // Thêm phương thức tìm kiếm theo Event và User
    Optional<EventAttendee> findByEventAndUser(Event event, User user);
}