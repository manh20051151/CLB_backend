package iuh.fit.backend.Event.dto.response;

import iuh.fit.backend.Event.enums.EventStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventResponse {
    String id;
    String name;
    String purpose;
    LocalDateTime time;
    String location;
    String content;
    EventStatus status;
    String createdBy; // Chỉ trả về ID của người tạo
    Set<AttendeeResponse> attendees;
//    Set<String> organizers;
    Set<OrganizerResponse> organizers;
//    Set<String> participants;
    Set<OrganizerResponse> participants;
    Set<String> permissions;
    String rejectionReason;
    Date createdAt;
}

