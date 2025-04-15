package iuh.fit.backend.identity.dto.request;

import iuh.fit.backend.identity.validator.DobConstraint;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCreateRequest {
    @Size(min = 3, message = "USERNAME_INVALID")
    String username;
    @Size(min = 8, message = "PASSWORD_INVALID")
    String password;
//    String studentCode;
    String firstName;
    String lastName;

    //    String avatar; // URL từ Cloudinary
    String email;
    Boolean gender; // True là nam
    @DobConstraint(min = 10, message = "INVALID_DOB")
    LocalDate dob;
}
