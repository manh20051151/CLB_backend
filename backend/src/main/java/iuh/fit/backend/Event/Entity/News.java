package iuh.fit.backend.Event.Entity;

import iuh.fit.backend.Event.enums.NewsStatus;
import iuh.fit.backend.Event.enums.NewsType;
import iuh.fit.backend.identity.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.Where;

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
@SQLDelete(sql = "UPDATE news SET deleted = true WHERE id = ?") // Câu lệnh SQL khi gọi delete
//@SQLRestriction("deleted = false") // Thay thế cho @Where
//@SQLRestriction("(SELECT u.locked FROM user u WHERE u.id = user_id) = false")
//@SQLRestriction("""
//    deleted = false AND
//    created_by IN (SELECT u.id FROM user u WHERE u.locked = false)
//    """)
@SQLRestriction("""
    deleted = false AND 
    created_by IN (SELECT u.id FROM user u WHERE u.locked = false) AND
    (event_id IS NULL OR event_id IN (SELECT e.id FROM event e WHERE e.deleted = false))
    """)
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


    @Column(name = "deleted", nullable = false)
    @Builder.Default
    boolean deleted = false; // Mặc định là false (chưa xóa)

    @Column(name = "deleted_at")
    @Temporal(TemporalType.TIMESTAMP)
    Date deletedAt; // Thời gian xóa

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deleted_by")
    User deletedBy; // Người thực hiện xóa


    @PrePersist
    protected void onCreate() {
        this.createdAt = new Date();
        if (this.status == null) {
            this.status = NewsStatus.PENDING;
        }
    }

    public void markAsDeleted(User deletedByUser) {
        this.deleted = true;
        this.deletedAt = new Date();
        this.deletedBy = deletedByUser;
    }
    public void reject(String reason) {
        this.status = NewsStatus.REJECTED;
        this.rejectionReason = reason;
        this.publishedAt = null;
    }

    public void approve() {
        this.status = NewsStatus.APPROVED;
        this.rejectionReason = null;
        this.publishedAt = LocalDateTime.now(); // Set thời gian publish khi approve
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof News news)) return false;
        return id != null && id.equals(news.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
