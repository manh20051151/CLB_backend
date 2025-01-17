package iuh.fit.backend.identity.mapper;


import iuh.fit.backend.identity.dto.request.PermissionRequest;
import iuh.fit.backend.identity.dto.request.RoleRequest;
import iuh.fit.backend.identity.dto.response.PermissionResponse;
import iuh.fit.backend.identity.dto.response.RoleResponse;
import iuh.fit.backend.identity.entity.Permission;
import iuh.fit.backend.identity.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    @Mapping(target = "permissions", ignore = true)
    Role toRole (RoleRequest request);
    RoleResponse toRoleResponse(Role role);

}
