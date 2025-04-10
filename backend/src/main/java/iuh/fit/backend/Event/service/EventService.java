package iuh.fit.backend.Event.service;

import com.corundumstudio.socketio.SocketIOServer;
import iuh.fit.backend.Event.Entity.*;
import iuh.fit.backend.Event.dto.request.EventCreateRequest;
import iuh.fit.backend.Event.dto.request.EventUpdateRequest;
import iuh.fit.backend.Event.dto.request.OrganizerRequest;
import iuh.fit.backend.Event.dto.response.AttendeeResponse;
import iuh.fit.backend.Event.dto.response.EventResponse;
import iuh.fit.backend.Event.dto.response.GroupChatResponse;
import iuh.fit.backend.Event.enums.EventStatus;
import iuh.fit.backend.Event.mapper.EventMapper;
import iuh.fit.backend.Event.repository.EventRepository;
import iuh.fit.backend.Event.repository.GroupChatRepository;
import iuh.fit.backend.Event.repository.OrganizerRoleRepository;
import iuh.fit.backend.Event.repository.PositionRepository;
import iuh.fit.backend.identity.entity.Permission;
import iuh.fit.backend.identity.entity.User;
import iuh.fit.backend.identity.exception.AppException;
import iuh.fit.backend.identity.exception.ErrorCode;
import iuh.fit.backend.identity.repository.PermissionRepository;
import iuh.fit.backend.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    private final PermissionRepository permissionRepository;

    private final PositionRepository positionRepository;
    private final OrganizerRoleRepository organizerRoleRepository;
    private final EventMapper eventMapper;
    private final GroupChatRepository groupChatRepository;

    private final SocketIOServer socketIOServer;
//    @Transactional
//    public EventResponse createEvent(EventCreateRequest request) {
//        validateEventRequest(request);
//
//        // 1. Tạo event cơ bản (chưa có organizers)
//        Event event = eventMapper.toEvent(request, userRepository, permissionRepository,
//                organizerRoleRepository, positionRepository);
//        event.setStatus(EventStatus.PENDING);
//
//        // 2. Lưu event trước để có ID
//        event = eventRepository.save(event);
//
//        // 3. Tạo organizers và thiết lập quan hệ với event
//        Set<EventOrganizer> organizers = new HashSet<>();
//        for (OrganizerRequest organizerRequest : request.getOrganizers()) {
//            User user = userRepository.findById(organizerRequest.getUserId())
//                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
//            OrganizerRole role = organizerRoleRepository.findById(organizerRequest.getRoleId())
//                    .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
//            Position position = positionRepository.findById(organizerRequest.getPositionId())
//                    .orElseThrow(() -> new AppException(ErrorCode.POSITION_NOT_FOUND));
//
//            EventOrganizer organizer = EventOrganizer.builder()
//                    .event(event) // Thiết lập quan hệ với event
//                    .user(user)
//                    .organizerRole(role)
//                    .position(position)
//                    .build();
//
//            organizers.add(organizer);
//        }
//
//        // 4. Tạo participants và thiết lập quan hệ với event
//        Set<EventParticipant> participants = new HashSet<>();
//        for (OrganizerRequest organizerRequest : request.getParticipants()) {
//            User user = userRepository.findById(organizerRequest.getUserId())
//                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
//            OrganizerRole role = organizerRoleRepository.findById(organizerRequest.getRoleId())
//                    .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
//            Position position = positionRepository.findById(organizerRequest.getPositionId())
//                    .orElseThrow(() -> new AppException(ErrorCode.POSITION_NOT_FOUND));
//            EventParticipant participant = EventParticipant.builder()
//                    .event(event) // Thiết lập quan hệ với event
//                    .user(user)
//                    .organizerRole(role)
//                    .position(position)
//                    .build();
//
//            participants.add(participant);
//        }
//
//        // 5. Thiết lập quan hệ hai chiều
//        event.setOrganizers(organizers);
//        event.setParticipants(participants);
//
//        // 6. Lưu lại event (cascade sẽ tự động lưu organizers)
//        event = eventRepository.save(event);
//
//        return eventMapper.toEventResponse(event);
//    }


    @Transactional
    public EventResponse createEvent(EventCreateRequest request) {
        validateEventRequest(request);

        // 1. Tạo event cơ bản (chưa có organizers)
        Event event = eventMapper.toEvent(request, userRepository, permissionRepository,
                organizerRoleRepository, positionRepository);
        event.setStatus(EventStatus.PENDING);

        // 2. Lưu event trước để có ID
        event = eventRepository.save(event);

        // 3. Tạo organizers và thiết lập quan hệ với event
        Set<EventOrganizer> organizers = new HashSet<>();
        Set<User> allMembers = new HashSet<>(); // Để dùng cho group chat

        for (OrganizerRequest organizerRequest : request.getOrganizers()) {
            User user = userRepository.findById(organizerRequest.getUserId())
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
            allMembers.add(user); // Thêm vào danh sách thành viên chat

            OrganizerRole role = organizerRoleRepository.findById(organizerRequest.getRoleId())
                    .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
            Position position = positionRepository.findById(organizerRequest.getPositionId())
                    .orElseThrow(() -> new AppException(ErrorCode.POSITION_NOT_FOUND));

            EventOrganizer organizer = EventOrganizer.builder()
                    .event(event)
                    .user(user)
                    .organizerRole(role)
                    .position(position)
                    .build();

            organizers.add(organizer);
        }

        // 4. Tạo participants và thiết lập quan hệ với event
        Set<EventParticipant> participants = new HashSet<>();
        for (OrganizerRequest organizerRequest : request.getParticipants()) {
            User user = userRepository.findById(organizerRequest.getUserId())
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
            allMembers.add(user); // Thêm vào danh sách thành viên chat

            OrganizerRole role = organizerRoleRepository.findById(organizerRequest.getRoleId())
                    .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
            Position position = positionRepository.findById(organizerRequest.getPositionId())
                    .orElseThrow(() -> new AppException(ErrorCode.POSITION_NOT_FOUND));
            EventParticipant participant = EventParticipant.builder()
                    .event(event)
                    .user(user)
                    .organizerRole(role)
                    .position(position)
                    .build();

            participants.add(participant);
        }

        // 5. Thiết lập quan hệ hai chiều
        event.setOrganizers(organizers);
        event.setParticipants(participants);

        // 6. Tạo group chat cho event
        User creator = userRepository.findById(request.getCreatedBy())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        GroupChat groupChat = new GroupChat();
        groupChat.setName(event.getName());
        groupChat.setEvent(event);
        groupChat.setGroupLeader(creator);
        groupChat.setMembers(allMembers);
        groupChat.setStatus(EventStatus.PENDING); // Đặt trạng thái ban đầu

        event.setGroupChat(groupChat);

        // 7. Lưu lại event (cascade sẽ tự động lưu organizers và group chat)
        event = eventRepository.save(event);

        return eventMapper.toEventResponse(event);
    }

    private void validateEventRequest(EventCreateRequest request) {
        if (request.getName() == null || request.getName().length() < 3) {
            throw new AppException(ErrorCode.INVALID_EVENT_NAME);
        }
        if (request.getPurpose() == null || request.getPurpose().length() < 10) {
            throw new AppException(ErrorCode.INVALID_EVENT_PURPOSE);
        }
        if (request.getContent() == null || request.getContent().length() < 20) {
            throw new AppException(ErrorCode.INVALID_EVENT_CONTENT);
        }
    }

    private Set<User> fetchUsersByIds(Set<String> userIds) {
        return userIds == null ? Set.of() :
                userRepository.findAllById(userIds).stream().collect(Collectors.toSet());
    }
//    @Transactional(readOnly = true)
    public List<EventResponse> getAllEvents() {
        List<Event> events = eventRepository.findAll();
        events.forEach(event -> Hibernate.initialize(event.getPermissions()));

        events.forEach(event -> {
            System.out.println("Event ID: " + event.getId());
            System.out.println("Permissions: " + event.getPermissions());
        });

        // 🛠️ Load permissions nếu chưa được fetch
//        events.forEach(event -> Hibernate.initialize(event.getPermissions()));
        return events.stream()
                .map(eventMapper::toEventResponse)
                .collect(Collectors.toList());
    }
    private Set<EventOrganizer> mapOrganizersByRequests(Set<OrganizerRequest> organizers, Event event) {
        if (organizers == null || organizers.isEmpty()) {
            return Collections.emptySet();
        }
        return organizers.stream().map(o -> {
            User user = userRepository.findById(o.getUserId())
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
            OrganizerRole role = organizerRoleRepository.findById(o.getRoleId())
                    .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
            Position position = positionRepository.findById(o.getPositionId())
                    .orElseThrow(() -> new AppException(ErrorCode.POSITION_NOT_FOUND));

            // Đảm bảo event được thiết lập
            return EventOrganizer.builder()
                    .event(event)
                    .user(user)
                    .organizerRole(role)
                    .position(position)
                    .build();
        }).collect(Collectors.toSet());
    }
    @Transactional(readOnly = true)
    public Optional<EventResponse> getEventById(String eventId) {
        return eventRepository.findById(eventId)
                .map(event -> {
                    Hibernate.initialize(event.getPermissions());
                    Hibernate.initialize(event.getOrganizers());
                    return eventMapper.toEventResponse(event);
                });
    }

    @Transactional(readOnly = true)
    public List<AttendeeResponse> getEventAttendees(String eventId, Boolean isAttending) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

        Stream<EventAttendee> attendeeStream = event.getAttendees().stream();

        // Lọc theo trạng thái tham dự nếu có
//        if (isAttending != null) {
//            attendeeStream = attendeeStream.filter(a -> a.isAttending() == isAttending);
//        }

        return attendeeStream
                .map(a -> new AttendeeResponse(
                        a.getUser().getId(),
                        a.getUser().getStudentCode(),
                        a.getUser().getFirstName(),
                        a.getUser().getLastName(),
                        a.isAttending()
                ))
                .collect(Collectors.toList());
    }
    @Transactional
    public EventResponse addAttendee(String eventId, String userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        EventAttendee attendee = new EventAttendee();
        attendee.setEvent(event);
        attendee.setUser(user);
        attendee.setAttending(true); // Mặc định tham gia

        event.getAttendees().add(attendee);


        // 4. Thêm user vào group chat của event (nếu có)
        if (event.getGroupChat() != null) {
            GroupChat groupChat = event.getGroupChat();

            // Kiểm tra xem user đã ở trong group chưa
            if (!groupChat.getMembers().contains(user)) {
                groupChat.getMembers().add(user);
                groupChatRepository.save(groupChat);
            }
        }

        event = eventRepository.save(event);

        return eventMapper.toEventResponse(event);
    }

    @Transactional
    public EventResponse removeAttendee(String eventId, String userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

        event.getAttendees().removeIf(attendee -> attendee.getUser().getId().equals(userId));

        event = eventRepository.save(event);
        return eventMapper.toEventResponse(event);
    }

    @Transactional
    public EventResponse updateAttendeeStatus(String eventId, String userId, boolean isAttending) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

        Optional<EventAttendee> optionalAttendee = event.getAttendees().stream()
                .filter(a -> a.getUser().getId().equals(userId))
                .findFirst();

        if (optionalAttendee.isPresent()) {
            EventAttendee attendee = optionalAttendee.get();
            attendee.setAttending(isAttending); // Chỉ cập nhật trạng thái của attendee đó
        } else {
            throw new AppException(ErrorCode.ATTENDEE_NOT_FOUND);
        }

        event = eventRepository.save(event);
        return eventMapper.toEventResponse(event);
    }

    @Transactional
    public EventResponse rejectEvent(String eventId, String reason) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

        event.reject(reason); // Cập nhật trạng thái & lý do từ chối

        // Cập nhật trạng thái group chat tương ứng
        if (event.getGroupChat() != null) {
            event.getGroupChat().reject();
        }

        event = eventRepository.save(event);

        return eventMapper.toEventResponse(event);
    }


    @Transactional
    public EventResponse approveEvent(String eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

        event.approve(); // Cập nhật trạng thái là APPROVED

        // Cập nhật trạng thái group chat tương ứng
        if (event.getGroupChat() != null) {
            event.getGroupChat().approve();
        }

        event = eventRepository.save(event);

        return eventMapper.toEventResponse(event);
    }

    public List<EventResponse> getEventsByStatus(EventStatus status) {
        List<Event> events;

        if (status != null) {
            events = eventRepository.findByStatus(status);
        } else {
            events = eventRepository.findAll();
        }

        events.forEach(event -> Hibernate.initialize(event.getPermissions()));

        return events.stream()
                .map(eventMapper::toEventResponse)
                .collect(Collectors.toList());
    }
    private void validateEventUpdateRequest(EventUpdateRequest request) {
        if (request.getName() == null || request.getName().length() < 3) {
            throw new AppException(ErrorCode.INVALID_EVENT_NAME);
        }
        if (request.getPurpose() == null || request.getPurpose().length() < 10) {
            throw new AppException(ErrorCode.INVALID_EVENT_PURPOSE);
        }
        if (request.getContent() == null || request.getContent().length() < 20) {
            throw new AppException(ErrorCode.INVALID_EVENT_CONTENT);
        }
    }
    public EventResponse updateEvent(String eventId, EventUpdateRequest request) {
        // 1. Tìm event hiện tại
        Event existingEvent = eventRepository.findById(eventId)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

        // 2. Validate request
        validateEventUpdateRequest(request);

        // 3. Cập nhật thông tin cơ bản
        existingEvent.setName(request.getName());
        existingEvent.setPurpose(request.getPurpose());
        existingEvent.setTime(request.getTime());
        existingEvent.setLocation(request.getLocation());
        existingEvent.setContent(request.getContent());

        // 4. Xử lý organizers - không clear() mà xóa từng phần tử riêng lẻ
        // Xóa organizers không còn trong request
        existingEvent.getOrganizers().removeIf(existingOrg ->
                request.getOrganizers().stream()
                        .noneMatch(reqOrg ->
                                reqOrg.getUserId().equals(existingOrg.getUser().getId()) &&
                                        reqOrg.getRoleId().equals(existingOrg.getOrganizerRole().getId()) &&
                                        reqOrg.getPositionId().equals(existingOrg.getPosition().getId())
                        )
        );

        // Thêm organizers mới
        for (OrganizerRequest organizerRequest : request.getOrganizers()) {
            boolean exists = existingEvent.getOrganizers().stream()
                    .anyMatch(org ->
                            organizerRequest.getUserId().equals(org.getUser().getId()) &&
                                    organizerRequest.getRoleId().equals(org.getOrganizerRole().getId()) &&
                                    organizerRequest.getPositionId().equals(org.getPosition().getId())
                    );

            if (!exists) {
                User user = userRepository.findById(organizerRequest.getUserId())
                        .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
                OrganizerRole role = organizerRoleRepository.findById(organizerRequest.getRoleId())
                        .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
                Position position = positionRepository.findById(organizerRequest.getPositionId())
                        .orElseThrow(() -> new AppException(ErrorCode.POSITION_NOT_FOUND));

                EventOrganizer organizer = EventOrganizer.builder()
                        .event(existingEvent)
                        .user(user)
                        .organizerRole(role)
                        .position(position)
                        .build();

                existingEvent.getOrganizers().add(organizer);
            }
        }

        // 5. Xử lý participants tương tự organizers
        existingEvent.getParticipants().removeIf(existingPart ->
                request.getParticipants().stream()
                        .noneMatch(reqPart ->
                                reqPart.getUserId().equals(existingPart.getUser().getId()) &&
                                        reqPart.getRoleId().equals(existingPart.getOrganizerRole().getId()) &&
                                        reqPart.getPositionId().equals(existingPart.getPosition().getId())
                        )
        );

        for (OrganizerRequest participantRequest : request.getParticipants()) {
            boolean exists = existingEvent.getParticipants().stream()
                    .anyMatch(part ->
                            participantRequest.getUserId().equals(part.getUser().getId()) &&
                                    participantRequest.getRoleId().equals(part.getOrganizerRole().getId()) &&
                                    participantRequest.getPositionId().equals(part.getPosition().getId())
                    );

            if (!exists) {
                User user = userRepository.findById(participantRequest.getUserId())
                        .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
                OrganizerRole role = organizerRoleRepository.findById(participantRequest.getRoleId())
                        .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
                Position position = positionRepository.findById(participantRequest.getPositionId())
                        .orElseThrow(() -> new AppException(ErrorCode.POSITION_NOT_FOUND));

                EventParticipant participant = EventParticipant.builder()
                        .event(existingEvent)
                        .user(user)
                        .organizerRole(role)
                        .position(position)
                        .build();

                existingEvent.getParticipants().add(participant);
            }
        }

        // 6. Lưu lại
        Event updatedEvent = eventRepository.save(existingEvent);
        return eventMapper.toEventResponse(updatedEvent);
    }

    public void deleteEvent(String eventId) {
        // 1. Kiểm tra tồn tại
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

        // 2. Xóa tất cả quan hệ trước (nếu cần)
        event.getOrganizers().clear();
        event.getParticipants().clear();
        eventRepository.save(event); // Xóa quan hệ trước khi xóa event

        // 3. Xóa event
        eventRepository.delete(event);
    }

    @Transactional(readOnly = true)
    public List<EventResponse> getEventsByCreator(String userId) {
        List<Event> events = eventRepository.findByCreatedBy_Id(userId);
        events.forEach(event -> Hibernate.initialize(event.getPermissions()));

        return events.stream()
                .map(eventMapper::toEventResponse)
                .collect(Collectors.toList());
    }

    ////

    // Lấy danh sách group chat đã approved theo user
    public List<GroupChatResponse> getApprovedGroupChatsByUser(String userId) {
        // Validate user exists
        if (!userRepository.existsById(userId)) {
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }

        return groupChatRepository.findByMembersIdAndStatus(userId, EventStatus.APPROVED)
                .stream()
                .map(this::convertToGroupChatResponse)
                .collect(Collectors.toList());
    }

    // Convert entity to DTO
    private GroupChatResponse convertToGroupChatResponse(GroupChat groupChat) {
        return GroupChatResponse.builder()
                .id(groupChat.getId())
                .name(groupChat.getName())
                .build();
    }

    public GroupChatResponse getGroupChatById(String groupChatId) {
        GroupChat groupChat = groupChatRepository.findById(groupChatId)
                .orElseThrow(() -> new AppException(ErrorCode.GROUP_CHAT_NOT_FOUND));

        return convertToDetailedGroupChatResponse(groupChat);
    }

    private GroupChatResponse convertToDetailedGroupChatResponse(GroupChat groupChat) {
        return GroupChatResponse.builder()
                .id(groupChat.getId())
                .name(groupChat.getName())
                .eventId(groupChat.getEvent() != null ? groupChat.getEvent().getId() : null)
                .groupLeaderId(groupChat.getGroupLeader() != null ? groupChat.getGroupLeader().getId() : null)
                .memberIds(groupChat.getMembers().stream()
                        .map(User::getId)
                        .collect(Collectors.toSet()))
                .status(groupChat.getStatus())
                .build();
    }

    @Transactional
    public GroupChatResponse removeMemberFromGroupChat(
            String groupChatId,
            String leaderId,
            String memberIdToRemove) {

        // 1. Lấy thông tin group chat
        GroupChat groupChat = groupChatRepository.findById(groupChatId)
                .orElseThrow(() -> new AppException(ErrorCode.GROUP_CHAT_NOT_FOUND));

        // 2. Kiểm tra quyền leader
        if (!groupChat.getGroupLeader().getId().equals(leaderId)) {
            throw new AppException(ErrorCode.PERMISSION_DENIED);
        }

        // 3. Tìm user cần xóa
        User userToRemove = userRepository.findById(memberIdToRemove)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // 4. Kiểm tra user có trong group không
        if (!groupChat.getMembers().contains(userToRemove)) {
            throw new AppException(ErrorCode.USER_NOT_IN_GROUP);
        }

        // 5. Không cho xóa chính leader
        if (memberIdToRemove.equals(leaderId)) {
            throw new AppException(ErrorCode.INVALID_OPERATION);
        }

        // 6. Thực hiện xóa
        groupChat.getMembers().remove(userToRemove);
        groupChat = groupChatRepository.save(groupChat);

        // 7. Gửi thông báo real-time


        return convertToGroupChatResponse(groupChat);
    }

    @Transactional
    public GroupChatResponse leaveGroupChat(String groupChatId, String memberId) {
        // 1. Lấy thông tin group chat
        GroupChat groupChat = groupChatRepository.findById(groupChatId)
                .orElseThrow(() -> new AppException(ErrorCode.GROUP_CHAT_NOT_FOUND));

        // 2. Tìm user muốn rời nhóm
        User leavingUser = userRepository.findById(memberId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // 3. Kiểm tra user có trong nhóm không
        if (!groupChat.getMembers().contains(leavingUser)) {
            throw new AppException(ErrorCode.USER_NOT_IN_GROUP);
        }

        // 4. Kiểm tra nếu là leader thì không được tự rời
        if (groupChat.getGroupLeader().getId().equals(memberId)) {
            throw new AppException(ErrorCode.LEADER_CANNOT_LEAVE);
        }

        // 5. Thực hiện rời nhóm
        groupChat.getMembers().remove(leavingUser);
        groupChat = groupChatRepository.save(groupChat);

        // 6. Gửi thông báo real-time

        return convertToGroupChatResponse(groupChat);
    }

    public GroupChatResponse deactivateGroup(String groupId, String leaderId) {

        GroupChat groupChat = groupChatRepository.findById(groupId)
                .orElseThrow(() -> new AppException(ErrorCode.GROUP_CHAT_NOT_FOUND));

        // 2. Kiểm tra quyền leader
        if (!groupChat.getGroupLeader().getId().equals(leaderId)) {
            throw new AppException(ErrorCode.PERMISSION_DENIED);
        }

        if (groupChat.getStatus() == EventStatus.PENDING) {
            throw new AppException(ErrorCode.INVALID_OPERATION);
        }

        groupChat.setStatus(EventStatus.PENDING);
        GroupChat updatedGroup = groupChatRepository.save(groupChat);

        sendSocketNotification(groupId, "group_deactivated",
                Map.of("newStatus", "PENDING"));

        return convertToGroupChatResponse(updatedGroup);
    }
    private void sendSocketNotification(String groupId, String event, Object data) {
        try {
            socketIOServer.getRoomOperations(groupId).sendEvent(event, data);
        } catch (Exception e) {
//            log.error("Failed to send socket notification", e);
        }
    }


}
