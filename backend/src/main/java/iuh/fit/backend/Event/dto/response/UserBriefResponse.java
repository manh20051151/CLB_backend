package iuh.fit.backend.Event.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserBriefResponse {
    private String id;
    private String username;
    private String firstName;
    private String lastName;
    private String avatar;
}
