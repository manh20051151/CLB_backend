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

    // Tìm tin nhắn theo nhóm và loại (IMAGE hoặc VIDEO)
    List<ChatMessage> findByGroupChatAndTypeInOrderBySentAtAsc(GroupChat groupChat, List<ChatMessage.MessageType> types);

    // Tìm tin nhắn theo nhóm và loại cụ thể (FILE hoặc AUDIO)
    List<ChatMessage> findByGroupChatAndTypeOrderBySentAtAsc(GroupChat groupChat, ChatMessage.MessageType type);

    // Tìm tin nhắn theo groupId và loại (IMAGE/VIDEO)
    List<ChatMessage> findByGroupChatIdAndTypeInOrderBySentAtAsc(String groupChatId, List<ChatMessage.MessageType> types);

    // Tìm tin nhắn theo groupId và loại cụ thể (FILE/AUDIO)
    List<ChatMessage> findByGroupChatIdAndTypeOrderBySentAtAsc(String groupChatId, ChatMessage.MessageType type);
}
