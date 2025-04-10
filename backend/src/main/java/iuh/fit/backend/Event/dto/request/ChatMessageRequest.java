package iuh.fit.backend.Event.dto.request;

import iuh.fit.backend.Event.Entity.ChatMessage;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatMessageRequest {
    private String senderId;
    private String content;
    private MultipartFile file;
    private ChatMessage.MessageType type;
    // getters & setters
}
