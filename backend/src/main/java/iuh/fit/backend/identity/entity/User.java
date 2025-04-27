package iuh.fit.backend.identity.entity;

import iuh.fit.backend.Event.Entity.OrganizerRole;
import iuh.fit.backend.Event.Entity.Position;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.Set;

@Entity
@Getter
@Setter
//@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
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


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return id != null && id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
