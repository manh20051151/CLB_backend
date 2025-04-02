package iuh.fit.backend.Event.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AttendeeResponse {
     String userId;
     String studentCode;
     String firstName;
     String lastName;
     boolean isAttending;

     public String getFullName() {
          return lastName + " " + firstName;
     }
}
