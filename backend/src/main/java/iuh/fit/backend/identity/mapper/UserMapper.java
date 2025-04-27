package iuh.fit.backend.identity.mapper;


import iuh.fit.backend.identity.dto.request.UserCreateRequest;
import iuh.fit.backend.identity.dto.request.UserUpdateByUserRequest;
import iuh.fit.backend.identity.dto.request.UserUpdateRequest;
import iuh.fit.backend.identity.dto.response.UserResponse;
import iuh.fit.backend.identity.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "avatar", ignore = true)
    User toUser (UserCreateRequest request);
    UserResponse toUserResponse(User user);

    @Mapping(target = "roles", ignore = true)
    void updateUser(@MappingTarget User user, UserUpdateRequest request);

    @Mapping(target = "roles", ignore = true)
    void updateUserByUser(@MappingTarget User user, UserUpdateByUserRequest request);
}
