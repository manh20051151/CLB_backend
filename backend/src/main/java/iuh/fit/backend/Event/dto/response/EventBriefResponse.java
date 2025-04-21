package iuh.fit.backend.Event.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventBriefResponse {
    private String id;
    private String name;
    private LocalDateTime time;
    private String location;
}
