package iuh.fit.backend.identity.entity;

import iuh.fit.backend.Event.Entity.OrganizerRole;
import iuh.fit.backend.Event.Entity.Position;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.util.Date;
import java.util.Set;

@Entity
@Getter
@Setter
//@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SQLRestriction("locked = false") // Tự động thêm điều kiện này vào các câu query
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    String username;
    String password;
//    String studentCode;
    String firstName;
    String lastName;
    LocalDate dob;

    String avatar; // URL từ Cloudinary
    String email;
    Boolean gender; // True là nam


    // Quan hệ Many-to-One với Position (optional = true mặc định)
    @ManyToOne
    @JoinColumn(name = "position_id")  // Tạo cột position_id trong bảng User
    Position position;  // Có thể null nếu User không thuộc Position nào

    @ManyToOne
    @JoinColumn(name = "organizer_role_id")
    OrganizerRole organizerRole;  // User có thể có một OrganizerRole hoặc không

    //    @ElementCollection
    @ManyToMany
    Set<Role> roles;

    @Column(nullable = false, columnDefinition = "boolean default false")
    @Builder.Default
    boolean locked = false; // Trạng thái khóa tài khoản

    @Column(name = "locked_at")
    @Temporal(TemporalType.TIMESTAMP)
    Date lockedAt; // Thời điểm khóa

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "locked_by")
    User lockedBy; // Người thực hiện khóa

    @Column(name = "lock_reason")
    String lockReason; // Lý do khóa

    @Column(name = "qr_code_url")
    private String qrCodeUrl; // URL hình ảnh QR code

    @Column(name = "qr_code_data")
    private String qrCodeData; // Dữ liệu được mã hóa trong QR code (thường là userId)


    @Column(name = "joined_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date joinedDate; // Ngày tham gia hệ thống

    // Phương thức khóa tài khoản
    public void lock(User lockedByUser, String reason) {
        this.locked = true;
        this.lockedAt = new Date();
        this.lockedBy = lockedByUser;
        this.lockReason = reason;
    }

    // Phương thức mở khóa tài khoản
    public void unlock() {
        this.locked = false;
        this.lockedAt = null;
        this.lockedBy = null;
        this.lockReason = null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return id != null && id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
