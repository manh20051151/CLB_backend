package iuh.fit.backend.Event.repository;

import iuh.fit.backend.Event.Entity.ChatMessage;
import iuh.fit.backend.Event.Entity.GroupChat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, String> {
    // Tìm tất cả tin nhắn trong group chat và sắp xếp theo thời gian gửi tăng dần
    List<ChatMessage> findByGroupChatOrderBySentAtAsc(GroupChat groupChat);

    // Hoặc nếu muốn tìm theo groupChatId
    List<ChatMessage> findByGroupChatIdOrderBySentAtAsc(String groupChatId);


}
