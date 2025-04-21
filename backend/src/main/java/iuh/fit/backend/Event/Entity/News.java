package iuh.fit.backend.Event.Entity;

import iuh.fit.backend.Event.enums.NewsStatus;
import iuh.fit.backend.Event.enums.NewsType;
import iuh.fit.backend.identity.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "news")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class News {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Column(nullable = false)
    String title; // Tiêu đề tin tức

    @Column(columnDefinition = "TEXT")
    String content; // Nội dung tin tức (có thể chứa HTML)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    Event event; // Tin tức có thể thuộc về 1 Event hoặc null nếu là tin độc lập

    @Column(name = "published_at")
    @Temporal(TemporalType.TIMESTAMP)
    LocalDateTime publishedAt; // Thời gian xuất bản

    @Column(name = "created_at", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    Date createdAt; // Thời gian tạo

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    User createdBy; // Người tạo tin tức

    @Enumerated(EnumType.STRING)
    NewsStatus status; // Trạng thái duyệt (giống EventStatus)

    String rejectionReason; // Lý do từ chối nếu có

    @Enumerated(EnumType.STRING)
    NewsType type; // Phân loại là NEWS hay RECAP

    @Column(name = "cover_image_url")
    String coverImageUrl; // URL ảnh bìa (nếu có)

    @Column(name = "is_featured")
    boolean featured; // Tin nổi bật

    @Column(name = "is_pinned")
    boolean pinned; // Tin ghim lên đầu

    @PrePersist
    protected void onCreate() {
        this.createdAt = new Date();
        if (this.status == null) {
            this.status = NewsStatus.PENDING;
        }
    }

    public void reject(String reason) {
        this.status = NewsStatus.REJECTED;
        this.rejectionReason = reason;
    }

    public void approve() {
        this.status = NewsStatus.APPROVED;
        this.rejectionReason = null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof News)) return false;
        News news = (News) o;
        return id != null && id.equals(news.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
