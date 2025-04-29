package iuh.fit.backend.identity.dto.response;

import iuh.fit.backend.Event.dto.response.OrganizerRoleResponse;
import iuh.fit.backend.Event.dto.response.PositionResponse;
import iuh.fit.backend.Event.dto.response.UserBriefResponse;
import iuh.fit.backend.identity.entity.Role;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.Date;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
    String id;
    String username;
//    String studentCode;
    String firstName;
    String lastName;
    LocalDate dob;
    Set<RoleResponse> roles;

    String avatar; // URL từ Cloudinary
    String email;
    Boolean gender; // True là nam
    PositionResponse position;
    OrganizerRoleResponse organizerRole;

    boolean locked;
    Date lockedAt;
    UserBriefResponse lockedBy;
    String lockReason;
}
