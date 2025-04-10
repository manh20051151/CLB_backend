package iuh.fit.backend.Event.repository;


import iuh.fit.backend.Event.Entity.GroupChat;
import iuh.fit.backend.Event.dto.response.GroupChatResponse;
import iuh.fit.backend.Event.enums.EventStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GroupChatRepository extends JpaRepository<GroupChat, String> {

    // Tìm group chat theo member và status
    List<GroupChat> findByMembersIdAndStatus(String memberId, EventStatus status);


}
