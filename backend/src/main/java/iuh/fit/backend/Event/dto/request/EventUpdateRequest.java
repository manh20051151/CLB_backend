package iuh.fit.backend.Event.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventUpdateRequest {
    String name;
    String purpose;
    LocalDateTime time;
    String location;
    String content;
    Set<OrganizerRequest> organizers;
    Set<OrganizerRequest> participants;
    Set<String> permissions;
}

