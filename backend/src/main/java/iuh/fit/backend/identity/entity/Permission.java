package iuh.fit.backend.identity.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import iuh.fit.backend.Event.Entity.Event;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.SQLRestriction;

import java.util.Objects;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Permission {
    @Id
    String name;

    String description;

    @ManyToMany(mappedBy = "permissions")
    @JsonIgnore // Ngăn serialization khi trả về JSON
    @ToString.Exclude // Loại bỏ khỏi toString()
    private Set<Event> events;

    // Implement equals và hashCode thủ công
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Permission that = (Permission) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    // toString() an toàn
    @Override
    public String toString() {
        return "Permission{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
