package iuh.fit.backend.Event.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NewsHistoryResponse {
    String id;
    String fieldName;
    String oldValue;
    String newValue;
    UserBriefResponse updatedBy;
    Date updatedAt;
}
