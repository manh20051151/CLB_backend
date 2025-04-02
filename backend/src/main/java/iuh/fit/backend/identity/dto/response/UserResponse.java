package iuh.fit.backend.identity.dto.response;

import iuh.fit.backend.identity.entity.Role;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
    String id;
    String username;
    String studentCode;
    String firstName;
    String lastName;
    LocalDate dob;
    Set<RoleResponse> roles;
}
