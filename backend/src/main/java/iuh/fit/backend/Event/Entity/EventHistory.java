package iuh.fit.backend.Event.Entity;

import iuh.fit.backend.identity.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.SQLRestriction;

import java.util.Date;

@Entity
@Table(name = "event_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@SQLRestriction("(SELECT u.locked FROM user u WHERE u.id = user_id) = false")
public class EventHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    Event event;

    @Column(nullable = false)
    String fieldName; // Tên trường thay đổi

    @Column(columnDefinition = "TEXT")
    String oldValue; // Giá trị cũ

    @Column(columnDefinition = "TEXT")
    String newValue; // Giá trị mới

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by", nullable = false)
    User updatedBy; // Người thực hiện thay đổi

    @Column(name = "updated_at", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    Date updatedAt; // Thời gian thay đổi

    @PrePersist
    protected void onCreate() {
        this.updatedAt = new Date();
    }
}
