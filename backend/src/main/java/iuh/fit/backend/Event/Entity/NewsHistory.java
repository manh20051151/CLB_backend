package iuh.fit.backend.Event.Entity;

import iuh.fit.backend.identity.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Entity
@Table(name = "news_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NewsHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "news_id", nullable = false)
    News news;

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
