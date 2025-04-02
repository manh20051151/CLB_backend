package iuh.fit.backend.Event.mapper;

import iuh.fit.backend.Event.Entity.OrganizerRole;
import iuh.fit.backend.Event.dto.request.OrganizerRoleRequest;
import iuh.fit.backend.Event.dto.response.OrganizerRoleResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrganizerRoleMapper {
    OrganizerRole toEntity(OrganizerRoleRequest request);

    OrganizerRoleResponse toResponse(OrganizerRole role);
}
