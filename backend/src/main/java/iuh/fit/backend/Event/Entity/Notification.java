package iuh.fit.backend.Event.Entity;

import iuh.fit.backend.Event.enums.NotificationType;
import iuh.fit.backend.identity.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.SQLRestriction;

import java.util.Date;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@SQLRestriction("(SELECT u.locked FROM user u WHERE u.id = user_id) = false")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user; // Người nhận thông báo

    @Column(nullable = false)
    String title; // Tiêu đề thông báo

    @Column(columnDefinition = "TEXT")
    String content; // Nội dung chi tiết

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    NotificationType type; // Loại thông báo

    @Column(name = "`read`", nullable = false)
    boolean read = false;

    @Column(name = "created_at", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    Date createdAt;

    @Column(name = "related_id")
    String relatedId; // ID của đối tượng liên quan (newsId)

    @PrePersist
    protected void onCreate() {
        this.createdAt = new Date();
    }
}


