package iuh.fit.backend.Event.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private String id;
    private String title;
    private String content;
    private String type;
    private boolean read;
    private Date createdAt;
    private String relatedId;
    private String userId;
}
