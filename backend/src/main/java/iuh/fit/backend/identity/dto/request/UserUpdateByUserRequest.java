package iuh.fit.backend.identity.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserUpdateByUserRequest {
    String passwordOld;
    String password;
//    String studentCode;
    String firstName;
    String lastName;
    LocalDate dob;
    String email;
    Boolean gender; // True là nam
}
