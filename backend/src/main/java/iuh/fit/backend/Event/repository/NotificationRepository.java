package iuh.fit.backend.Event.repository;

import iuh.fit.backend.Event.Entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, String> {
    // Lấy N thông báo mới nhất (không phân trang)
    @Query(value = "SELECT * FROM notifications WHERE user_id = :userId ORDER BY created_at DESC LIMIT :limit",
            nativeQuery = true)
    List<Notification> findLatestNotifications(@Param("userId") String userId, @Param("limit") int limit);

    List<Notification> findByUserIdOrderByCreatedAtDesc(String userId);
    Long countByUserIdAndReadFalse(String userId);
}
