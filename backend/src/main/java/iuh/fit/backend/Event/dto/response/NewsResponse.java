package iuh.fit.backend.Event.dto.response;

import iuh.fit.backend.Event.enums.NewsStatus;
import iuh.fit.backend.Event.enums.NewsType;
import iuh.fit.backend.identity.dto.response.UserResponse;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@Builder
public class NewsResponse {

    private String id;
    private String title;
    private String content;
    private String coverImageUrl;
    private LocalDateTime publishedAt;
    private Date createdAt;
    private NewsStatus status;
    private String rejectionReason;
    private NewsType type;
    private boolean featured;
    private boolean pinned;
    private UserBriefResponse createdBy;
    private EventBriefResponse event;

    private boolean deleted;
    private Date deletedAt;
    private UserBriefResponse deletedBy;
}
