package iuh.fit.backend.Event.Entity;

import iuh.fit.backend.identity.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Position {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    String name;

    // Quan hệ One-to-Many với User (tùy chọn, nếu cần truy vấn ngược)
    @OneToMany(mappedBy = "position")
    Set<User> users = new HashSet<>();
}
