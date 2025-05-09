package iuh.fit.backend.Event.repository;

import iuh.fit.backend.Event.Entity.Event;
import iuh.fit.backend.Event.enums.EventStatus;
import iuh.fit.backend.identity.entity.Permission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface EventRepository extends JpaRepository<Event, String> {
    List<Event> findByStatus(EventStatus status);
    List<Event> findByCreatedBy_Id(String userId);
    int DEFAULT_DURATION_MINUTES = 1440; // 1 ngày


    @Query("SELECT DISTINCT e FROM Event e " +
            "JOIN e.attendees a " +
            "WHERE a.user.id = :userId")
    List<Event> findByAttendeeId(@Param("userId") String userId);

    // Thêm method này để query các event đã xóa (ghi đè @Where clause)


    @Query(value = "SELECT * FROM event WHERE deleted = true", nativeQuery = true)
    Page<Event> findByDeletedTrue(Pageable pageable);

    @Query(value = "SELECT * FROM event WHERE id = ?1 AND deleted = true", nativeQuery = true)
    Optional<Event> findDeletedEventById(String eventId);


    @Query(value = "SELECT e.* FROM event e WHERE " +
            "e.deleted = false AND " +
            "e.created_by IN (SELECT u.id FROM user u WHERE u.locked = false) AND " +
            "e.progress_status <> 'COMPLETED' AND " +
            "((e.time BETWEEN ?1 AND ?2) OR " +
            "(e.time <= ?2 AND DATE_ADD(e.time, INTERVAL " + DEFAULT_DURATION_MINUTES + " MINUTE) >= ?1))",
            nativeQuery = true)
    List<Event> findEventsForStatusUpdate(
            LocalDateTime startTime,
            LocalDateTime endTime
    );

    // Phương thức tìm event đã kết thúc
    @Query("SELECT e FROM Event e WHERE " +
            "e.progressStatus <> 'COMPLETED' AND " +
            "e.time < :cutoffTime")
    List<Event> findEventsToComplete(
            @Param("cutoffTime") LocalDateTime cutoffTime
    );


}
