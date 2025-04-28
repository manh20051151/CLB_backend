package iuh.fit.backend.Event.mapper;

import iuh.fit.backend.Event.Entity.Notification;
import iuh.fit.backend.Event.dto.response.NotificationResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NotificationMapper {
    NotificationResponse toResponse(Notification notification);
}
