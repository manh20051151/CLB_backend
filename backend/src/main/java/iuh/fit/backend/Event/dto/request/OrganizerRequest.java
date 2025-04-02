package iuh.fit.backend.Event.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrganizerRequest {
    @NotNull(message = "USER_ID_REQUIRED")
    String userId;

    @NotNull(message = "ROLE_ID_REQUIRED")
    String roleId;

    @NotNull(message = "POSITION_ID_REQUIRED")
    String positionId;
}
