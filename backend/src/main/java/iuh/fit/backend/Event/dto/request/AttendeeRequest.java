package iuh.fit.backend.Event.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AttendeeRequest {
    private String userId;
    private boolean isAttending;
}
