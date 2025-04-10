package iuh.fit.backend.Event.dto.response;


import iuh.fit.backend.Event.enums.EventStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GroupChatResponse {
    private String id;
    private String name;

    private String eventId;
    private String groupLeaderId;
    private Set<String> memberIds;
    private EventStatus status;
}
