package iuh.fit.backend.Event.Entity;



import iuh.fit.backend.Event.enums.EventStatus;
import iuh.fit.backend.identity.entity.Permission;
import iuh.fit.backend.identity.entity.Role;
import iuh.fit.backend.identity.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

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

    @Enumerated(EnumType.STRING)
    EventStatus status; // Trạng thái duyệt

    String rejectionReason; // 💡 Lý do từ chối

    @Column(name = "created_at", nullable = true, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt; // Thêm trường mới
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

    public void reject(String reason) {
        this.status = EventStatus.REJECTED;
        this.rejectionReason = reason;
    }
    public void approve() {
        this.status = EventStatus.APPROVED;
        this.rejectionReason = null; // Xóa lý do từ chối nếu có
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
