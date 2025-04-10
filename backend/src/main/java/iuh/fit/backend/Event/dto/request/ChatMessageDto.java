package iuh.fit.backend.Event.dto.request;

import iuh.fit.backend.Event.Entity.ChatMessage;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatMessageDto {
    private String id;
    private String content;
    private String senderId;
    private String senderName;
    private Date sentAt;
    private ChatMessage.MessageType type;
    private String fileUrl;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private boolean deleted;

    // Getters and setters
}
