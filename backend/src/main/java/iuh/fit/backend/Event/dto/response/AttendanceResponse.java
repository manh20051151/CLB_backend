package iuh.fit.backend.Event.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceResponse {
    private String eventId;
    private String eventName;
    private String attendeeId;
    private String attendeeName;
    private LocalDateTime checkedInAt;
}
