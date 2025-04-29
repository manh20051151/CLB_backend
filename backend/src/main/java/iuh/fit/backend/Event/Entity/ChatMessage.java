package iuh.fit.backend.Event.Entity;

import iuh.fit.backend.identity.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.SQLRestriction;

import java.util.Date;

@Entity
@Getter
@Setter
//@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "chat_messages")
@SQLRestriction("(SELECT u.locked FROM user u WHERE u.id = user_id) = false")
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = true) // Cho phép null khi là file
    private String content;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne
    @JoinColumn(name = "group_chat_id", nullable = false)
    private GroupChat groupChat;

    @Column(name = "sent_at", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date sentAt;

    @Enumerated(EnumType.STRING)
    private MessageType type; // TEXT, IMAGE, FILE, etc.

    private String fileUrl; // URL từ Cloudinary
    private String fileName; // Tên file gốc
    private String fileType; // Loại file (MIME type)
    private Long fileSize; // Kích thước file (bytes)

    @Column(nullable = false)
    private boolean deleted = false;

    @PrePersist
    protected void onCreate() {
        this.sentAt = new Date();
        if (this.type == null) {
            this.type = MessageType.TEXT;
        }
    }

    public enum MessageType {
        TEXT, IMAGE, FILE, VIDEO, AUDIO
    }
    // Getters and setters
}
