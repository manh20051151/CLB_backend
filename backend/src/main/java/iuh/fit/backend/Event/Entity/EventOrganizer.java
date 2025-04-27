package iuh.fit.backend.Event.Entity;

import iuh.fit.backend.identity.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
//@Data
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)

public class EventOrganizer {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

//    @ManyToOne
//    @JoinColumn(name = "event_id", nullable = false)
//    private Event event;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(optional = true)
    @JoinColumn(name = "organizer_role_id", nullable = true)
    private OrganizerRole organizerRole;

    @ManyToOne(optional = true)
    @JoinColumn(name = "position_id", nullable = true)
    private Position position;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EventOrganizer)) return false;
        EventOrganizer that = (EventOrganizer) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
