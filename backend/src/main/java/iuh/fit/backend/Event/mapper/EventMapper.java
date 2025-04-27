package iuh.fit.backend.Event.mapper;

import iuh.fit.backend.Event.Entity.*;
import iuh.fit.backend.Event.dto.request.EventCreateRequest;
import iuh.fit.backend.Event.dto.request.OrganizerRequest;
import iuh.fit.backend.Event.dto.response.AttendeeResponse;
import iuh.fit.backend.Event.dto.response.EventResponse;
import iuh.fit.backend.Event.dto.response.OrganizerResponse;
import iuh.fit.backend.Event.repository.OrganizerRoleRepository;
import iuh.fit.backend.Event.repository.PositionRepository;
import iuh.fit.backend.identity.entity.Permission;
import iuh.fit.backend.identity.entity.User;
import iuh.fit.backend.identity.exception.AppException;
import iuh.fit.backend.identity.exception.ErrorCode;
import iuh.fit.backend.identity.mapper.UserMapper;
import iuh.fit.backend.identity.repository.PermissionRepository;
import iuh.fit.backend.identity.repository.UserRepository;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface EventMapper {

    @Mapping(target = "createdBy", source = "createdBy", qualifiedByName = "mapUserById")
//    @Mapping(target = "attendees", source = "attendees", qualifiedByName = "mapUsersByIds")
//    @Mapping(target = "organizers", source = "organizers", qualifiedByName = "mapUsersByIds")
//    @Mapping(target = "organizers", source = "organizers", qualifiedByName = "mapOrganizersByRequests") // Chuyển OrganizerRequest → EventOrganizer

//    @Mapping(target = "participants", source = "participants", qualifiedByName = "mapUsersByIds")
    @Mapping(target = "permissions", source = "permissions", qualifiedByName = "mapPermissionsByNames") // 💡 Mapping mới
    @Mapping(target = "organizers", ignore = true)
    @Mapping(target = "participants", ignore = true)
    @Mapping(target = "attendees", ignore = true)
    Event toEvent(EventCreateRequest request,
                  @Context UserRepository userRepository,
                  @Context PermissionRepository permissionRepository,
                  @Context OrganizerRoleRepository roleRepository,
                  @Context PositionRepository positionRepository);

    @Mapping(target = "createdBy", source = "createdBy", qualifiedByName = "mapUserToId")
//    @Mapping(target = "attendees", source = "attendees", qualifiedByName = "mapUsersToIds")
//    @Mapping(target = "organizers", source = "organizers", qualifiedByName = "mapUsersToIds")
    @Mapping(target = "organizers", source = "organizers", qualifiedByName = "mapOrganizersToResponses") // Chuyển EventOrganizer → OrganizerResponse
    @Mapping(target = "participants", source = "participants", qualifiedByName = "mapParticipantsToResponses") // Chuyển EventOrganizer → OrganizerResponse
    @Mapping(target = "attendees", source = "attendees", qualifiedByName = "mapAttendeesToResponses")
//    @Mapping(target = "participants", source = "participants", qualifiedByName = "mapUsersToIds")
    @Mapping(target = "permissions", source = "permissions", qualifiedByName = "mapPermissionsToNames")
    EventResponse toEventResponse(Event event);

    // Chuyển từ String (userId) → User entity
    @Named("mapUserById")
    default User mapUserById(String userId, @Context UserRepository userRepository) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    // Chuyển từ Set<String> (userIds) → Set<User>
    @Named("mapUsersByIds")
    default Set<User> mapUsersByIds(Set<String> userIds, @Context UserRepository userRepository) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptySet();
        }
        return new HashSet<>(userRepository.findAllById(userIds));
    }

    // Chuyển từ User entity → String (userId)
    @Named("mapUserToId")
    default String mapUserToId(User user) {
        return user != null ? user.getId() : null;
    }

    // Chuyển từ Set<User> → Set<String> (userIds)
    @Named("mapUsersToIds")
    default Set<String> mapUsersToIds(Set<User> users) {
        if (users == null || users.isEmpty()) {
            return Collections.emptySet();
        }
        return users.stream().map(User::getId).collect(Collectors.toSet());
    }
    // 💡 Chuyển từ Set<String> (permission names) → Set<Permission>
    @Named("mapPermissionsByNames")
    default Set<Permission> mapPermissionsByNames(Set<String> permissionNames, @Context PermissionRepository permissionRepository) {
        if (permissionNames == null || permissionNames.isEmpty()) {
            return Collections.emptySet();
        }
        return new HashSet<>(permissionRepository.findByNameIn(permissionNames));
    }

    // 💡 Chuyển từ Set<Permission> → Set<String> (permission names)
    @Named("mapPermissionsToNames")
    default Set<String> mapPermissionsToNames(Set<Permission> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return Collections.emptySet();
        }
        return permissions.stream().map(Permission::getName).collect(Collectors.toSet());
    }

    @Named("mapOrganizersByRequests")
    default Set<EventOrganizer> mapOrganizersByRequests(
            Set<OrganizerRequest> organizers,
            @Context UserRepository userRepository,
            @Context OrganizerRoleRepository roleRepository,
            @Context PositionRepository positionRepository) {

        if (organizers == null || organizers.isEmpty()) {
            return Collections.emptySet();
        }

        return organizers.stream().map(o -> {
            User user = userRepository.findById(o.getUserId())
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
            OrganizerRole role = roleRepository.findById(o.getRoleId())
                    .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
            Position position = positionRepository.findById(o.getPositionId())
                    .orElseThrow(() -> new AppException(ErrorCode.POSITION_NOT_FOUND));

            return EventOrganizer.builder()
                    .user(user)
                    .organizerRole(role)
                    .position(position)
                    .build();
        }).collect(Collectors.toSet());
    }
//    @Named("mapAttendeesToResponses")
//    default Set<AttendeeResponse> mapAttendeesToResponses(Set<EventAttendee> attendees) {
//        if (attendees == null || attendees.isEmpty()) {
//            return Collections.emptySet();
//        }
//        return attendees.stream().map(a ->
//                new AttendeeResponse(a.getUser().getId(), a.isAttending())
//        ).collect(Collectors.toSet());
//    }

    @Named("mapAttendeesToResponses")
    default Set<AttendeeResponse> mapAttendeesToResponses(Set<EventAttendee> attendees) {
        if (attendees == null || attendees.isEmpty()) {
            return Collections.emptySet();
        }
        return attendees.stream().map(a -> {
            User user = a.getUser();
            return new AttendeeResponse(
                    user.getId(),
                    user.getUsername(),
                    user.getFirstName(),
                    user.getLastName(),
                    a.isAttending()
            );
        }).collect(Collectors.toSet());
    }
    // Ánh xạ từ EventOrganizer → OrganizerResponse
//    @Named("mapOrganizersToResponses")
//    default Set<OrganizerResponse> mapOrganizersToResponses(Set<EventOrganizer> organizers) {
//        if (organizers == null || organizers.isEmpty()) {
//            return Collections.emptySet();
//        }
//        return organizers.stream().map(o ->
//                new OrganizerResponse(o.getUser().getId(), o.getOrganizerRole().getName(), o.getPosition().getName())
//        ).collect(Collectors.toSet());
//    }

    @Named("mapOrganizersToResponses")
    default Set<OrganizerResponse> mapOrganizersToResponses(Set<EventOrganizer> organizers) {
        if (organizers == null || organizers.isEmpty()) {
            return Collections.emptySet();
        }

        return organizers.stream()
                .map(o -> {
                    String roleName = o.getOrganizerRole() != null ? o.getOrganizerRole().getName() : null;
                    String positionName = o.getPosition() != null ? o.getPosition().getName() : null;

                    return new OrganizerResponse(
                            o.getUser().getId(),
                            roleName,
                            positionName
                    );
                })
                .collect(Collectors.toSet());
    }

    // Ánh xạ từ EventOrganizer → OrganizerResponse
    @Named("mapParticipantsToResponses")
    default Set<OrganizerResponse> mapParticipantsToResponses(Set<EventParticipant> participants) {
        if (participants == null || participants.isEmpty()) {
            return Collections.emptySet();
        }
        return participants.stream().map(o ->
                new OrganizerResponse(o.getUser().getId(), o.getOrganizerRole().getName(), o.getPosition().getName())
        ).collect(Collectors.toSet());
    }


}

