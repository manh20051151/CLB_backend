package iuh.fit.backend.Event.mapper;

import iuh.fit.backend.Event.Entity.Event;
import iuh.fit.backend.Event.Entity.News;
import iuh.fit.backend.Event.dto.request.NewsCreateRequest;
import iuh.fit.backend.Event.dto.response.EventBriefResponse;
import iuh.fit.backend.Event.dto.response.NewsResponse;
import iuh.fit.backend.Event.dto.response.UserBriefResponse;
import iuh.fit.backend.identity.entity.User;
import iuh.fit.backend.identity.mapper.UserMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NewsMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "rejectionReason", ignore = true)
    @Mapping(target = "coverImageUrl", ignore = true)
    @Mapping(target = "event", source = "eventId")
    @Mapping(target = "createdBy", source = "createdById")
    News toNews(NewsCreateRequest request);

    NewsResponse toNewsResponse(News news);

    default Event mapEventIdToEvent(String eventId) {
        if (eventId == null) {
            return null;
        }
        return Event.builder().id(eventId).build();
    }

    default User mapUserIdToUser(String userId) {
        if (userId == null) {
            return null;
        }
        return User.builder().id(userId).build();
    }

    UserBriefResponse toUserBriefResponse(User user);
    EventBriefResponse toEventBriefResponse(Event event);
}
