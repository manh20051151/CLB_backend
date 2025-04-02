package iuh.fit.backend.Event.Entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "organizer_roles")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrganizerRole {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    String name;
}
