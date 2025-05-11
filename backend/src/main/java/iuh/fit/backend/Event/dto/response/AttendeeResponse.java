package iuh.fit.backend.Event.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

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
     Boolean isAttending;

     LocalDateTime checkedInAt;

     public String getFullName() {
          return lastName + " " + firstName;
     }
}
