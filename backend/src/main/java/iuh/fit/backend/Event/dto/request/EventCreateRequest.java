package iuh.fit.backend.Event.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventCreateRequest {

//    @Size(min = 3, message = "EVENT_NAME_INVALID")
    String name;

//    @Size(min = 10, message = "EVENT_PURPOSE_INVALID")
    String purpose;

//    @NotNull(message = "EVENT_TIME_REQUIRED")
    LocalDateTime time;

//    @Size(min = 5, message = "EVENT_LOCATION_INVALID")
    String location;

//    @Size(min = 20, message = "EVENT_CONTENT_INVALID")
    String content;

    @NotNull(message = "EVENT_CREATOR_REQUIRED")
    String createdBy; // ID của người tạo sự kiện

//    Set<String> attendees;    // Danh sách ID của người tham gia
    Set<AttendeeRequest> attendees;
//    Set<String> organizers;   // Danh sách ID của ban tổ chức
    Set<OrganizerRequest> organizers;
    //    Set<String> participants; // Danh sách ID của thành phần tham dự
    Set<OrganizerRequest> participants; // Danh sách ID của thành phần tham dự
    Set<String> permissions;
}

