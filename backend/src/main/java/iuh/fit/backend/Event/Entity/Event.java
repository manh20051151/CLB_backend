package iuh.fit.backend.Event.Entity;



import iuh.fit.backend.Event.enums.EventProgressStatus;
import iuh.fit.backend.Event.enums.EventStatus;
import iuh.fit.backend.identity.entity.Permission;
import iuh.fit.backend.identity.entity.Role;
import iuh.fit.backend.identity.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.Where;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
//@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SQLDelete(sql = "UPDATE event SET deleted = true WHERE id = ?") // Câu lệnh SQL khi gọi delete
//@SQLRestriction("deleted = false") // Thay thế cho @Where // Tự động thêm điều kiện này vào các câu query
//@SQLRestriction("deleted = false AND (SELECT u.locked FROM user u WHERE u.id = created_by_id) = false")
@SQLRestriction("""
    deleted = false AND 
    created_by IN (SELECT u.id FROM user u WHERE u.locked = false)
    """)
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    String name;
    String purpose;

    @Temporal(TemporalType.TIMESTAMP)
    LocalDateTime time;

    String location;

    String content;
    @Column(name = "avatar_url")
    @Builder.Default
    private String avatarUrl = "https://res.cloudinary.com/dnvtmbmne/image/upload/v1745957726/gk1e6knghh2sqhp8tbbn.png"; // URL mặc định

    @Enumerated(EnumType.STRING)
    EventStatus status; // Trạng thái duyệt

    String rejectionReason; // 💡 Lý do từ chối

    @Column(name = "created_at", nullable = true, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    User createdBy; // Người tạo sự kiện

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<EventAttendee> attendees = new HashSet<>();

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<EventOrganizer> organizers = new HashSet<>();

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<EventParticipant> participants = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinTable(
            name = "event_permissions",
            joinColumns = @JoinColumn(name = "event_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_name") // Thay vì permission_id, ta dùng name
    )
    private Set<Permission> permissions;

    @OneToOne(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private GroupChat groupChat;


    // Thêm quan hệ với News
    @OneToMany(mappedBy = "event", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = false)
    Set<News> news = new HashSet<>();


    @Column(name = "deleted", nullable = false)
    @Builder.Default
    boolean deleted = false; // Mặc định là false (chưa xóa)

    @Column(name = "deleted_at")
    @Temporal(TemporalType.TIMESTAMP)
    Date deletedAt; // Thời gian xóa

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deleted_by")
    User deletedBy; // Người thực hiện xóa


    @Enumerated(EnumType.STRING)
    @Column(name = "progress_status")
    private EventProgressStatus progressStatus;

    @Column(name = "qr_code_url")
    private String qrCodeUrl; // URL hình ảnh QR code

    @Column(name = "qr_code_data")
    private String qrCodeData; // Dữ liệu được mã hóa trong QR code (thường là userId)


    public boolean shouldUpdateStatus() {
        // Chỉ cập nhật nếu chưa ở trạng thái COMPLETED
        return this.progressStatus != EventProgressStatus.COMPLETED;
    }

    public void updateProgressStatus() {
        if (!shouldUpdateStatus() || this.time == null) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        int defaultDuration = 1440; // 1 ngày
        LocalDateTime endTime = this.time.plusMinutes(defaultDuration);

        if (now.isBefore(this.time)) {
            this.progressStatus = EventProgressStatus.UPCOMING;
        } else if (now.isBefore(endTime)) {
            this.progressStatus = EventProgressStatus.ONGOING;
        } else {
            this.progressStatus = EventProgressStatus.COMPLETED;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updateProgressStatus();
    }

    public void reject(String reason) {
        this.status = EventStatus.REJECTED;
        this.rejectionReason = reason;
    }
    public void approve() {
        this.status = EventStatus.APPROVED;
        this.rejectionReason = null; // Xóa lý do từ chối nếu có
    }

    // Thêm phương thức helper để quản lý quan hệ với News
    public void addNews(News newsItem) {
        this.news.add(newsItem);
        newsItem.setEvent(this);
    }

    public void removeNews(News newsItem) {
        this.news.remove(newsItem);
        newsItem.setEvent(null);
    }

    // Thêm phương thức markAsDeleted
    public void markAsDeleted(User deletedByUser) {
        this.deleted = true;
        this.deletedAt = new Date();
        this.deletedBy = deletedByUser;

        // Nếu cần, có thể đặt lại status khi xóa
//        this.status = EventStatus.CANCELLED;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = new Date(); // Tự động gán thời gian hiện tại khi tạo mới
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Event)) return false;
        Event event = (Event) o;
        return id != null && id.equals(event.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
    public void addOrganizer(EventOrganizer organizer, EventParticipant participant, EventAttendee eventAttendee) {
        this.organizers.add(organizer);
        this.participants.add(participant);
        this.attendees.add(eventAttendee);
        organizer.setEvent(this);
        participant.setEvent(this);
        eventAttendee.setEvent(this);
    }
}
