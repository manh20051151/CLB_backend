package iuh.fit.backend.identity.mapper;


import iuh.fit.backend.identity.dto.request.PermissionRequest;
import iuh.fit.backend.identity.dto.request.UserCreateRequest;
import iuh.fit.backend.identity.dto.request.UserUpdateRequest;
import iuh.fit.backend.identity.dto.response.PermissionResponse;
import iuh.fit.backend.identity.dto.response.UserResponse;
import iuh.fit.backend.identity.entity.Permission;
import iuh.fit.backend.identity.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    Permission toPermission (PermissionRequest  request);
    PermissionResponse toPermissionResponse(Permission permission);

}
