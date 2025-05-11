package iuh.fit.backend.Event.service;

import com.corundumstudio.socketio.SocketIOServer;
import iuh.fit.backend.Event.Entity.*;
import iuh.fit.backend.Event.dto.request.EventCreateRequest;
import iuh.fit.backend.Event.dto.request.EventUpdateRequest;
import iuh.fit.backend.Event.dto.request.OrganizerRequest;
import iuh.fit.backend.Event.dto.response.*;
import iuh.fit.backend.Event.enums.EventProgressStatus;
import iuh.fit.backend.Event.enums.EventStatus;
import iuh.fit.backend.Event.enums.NotificationType;
import iuh.fit.backend.Event.mapper.EventMapper;
import iuh.fit.backend.Event.mapper.NewsMapper;
import iuh.fit.backend.Event.repository.*;
import iuh.fit.backend.identity.entity.User;
import iuh.fit.backend.identity.exception.AppException;
import iuh.fit.backend.identity.exception.ErrorCode;
import iuh.fit.backend.identity.repository.PermissionRepository;
import iuh.fit.backend.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventService {
    private final EventAttendeeRepository eventAttendeeRepository;
    private final EventHistoryRepository eventHistoryRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    private final PermissionRepository permissionRepository;

    private final PositionRepository positionRepository;
    private final OrganizerRoleRepository organizerRoleRepository;
    private final EventMapper eventMapper;
    private final GroupChatRepository groupChatRepository;

    private final SocketIOServer socketIOServer;
    private final NotificationRepository notificationRepository;

    private final NewsMapper newsMapper;
    private final CloudinaryService cloudinaryService;
    private static final int DEFAULT_DURATION_MINUTES = 1440; // 1 ngày

    private final QrCodeService qrCodeService;
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
//        Set<User> allMembers = new HashSet<>(); // Để dùng cho group chat
//
//        for (OrganizerRequest organizerRequest : request.getOrganizers()) {
//            User user = userRepository.findById(organizerRequest.getUserId())
//                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
//            allMembers.add(user); // Thêm vào danh sách thành viên chat
//
//            OrganizerRole role = organizerRoleRepository.findById(organizerRequest.getRoleId())
//                    .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
//            Position position = positionRepository.findById(organizerRequest.getPositionId())
//                    .orElseThrow(() -> new AppException(ErrorCode.POSITION_NOT_FOUND));
//
//            EventOrganizer organizer = EventOrganizer.builder()
//                    .event(event)
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
//            allMembers.add(user); // Thêm vào danh sách thành viên chat
//
//            OrganizerRole role = organizerRoleRepository.findById(organizerRequest.getRoleId())
//                    .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
//            Position position = positionRepository.findById(organizerRequest.getPositionId())
//                    .orElseThrow(() -> new AppException(ErrorCode.POSITION_NOT_FOUND));
//            EventParticipant participant = EventParticipant.builder()
//                    .event(event)
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
//        // 6. Tạo group chat cho event
//        User creator = userRepository.findById(request.getCreatedBy())
//                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
//
//        GroupChat groupChat = new GroupChat();
//        groupChat.setName(event.getName());
//        groupChat.setEvent(event);
//        groupChat.setGroupLeader(creator);
//        groupChat.setMembers(allMembers);
//        groupChat.setStatus(EventStatus.PENDING); // Đặt trạng thái ban đầu
//
//        event.setGroupChat(groupChat);
//
//        // 7. Lưu lại event (cascade sẽ tự động lưu organizers và group chat)
//        event = eventRepository.save(event);
//
//        return eventMapper.toEventResponse(event);
//    }

    @Transactional
    public EventResponse createEvent(EventCreateRequest request) throws IOException {
        validateEventRequest(request);

        // 1. Tạo event cơ bản (chưa có organizers)
        Event event = eventMapper.toEvent(request, userRepository, permissionRepository,
                organizerRoleRepository, positionRepository);
        event.setStatus(EventStatus.PENDING);

        // 2. Lưu event trước để có ID
        event = eventRepository.save(event);

        User userCreatedBy = userRepository.findById(request.getCreatedBy())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        // 3. Tạo organizers và thiết lập quan hệ với event
        Set<EventOrganizer> organizers = new HashSet<>();
        Set<User> allMembers = new HashSet<>(); // Để dùng cho group chat
        allMembers.add(userCreatedBy);
        for (OrganizerRequest organizerRequest : request.getOrganizers()) {
            User user = userRepository.findById(organizerRequest.getUserId())
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
            allMembers.add(user); // Thêm vào danh sách thành viên chat

            OrganizerRole role = organizerRoleRepository.findById(organizerRequest.getRoleId())
                    .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
//            Position position = positionRepository.findById(organizerRequest.getPositionId())
//                    .orElseThrow(() -> new AppException(ErrorCode.POSITION_NOT_FOUND));

            EventOrganizer organizer = EventOrganizer.builder()
                    .event(event)
                    .user(user)
                    .organizerRole(user.getOrganizerRole())
                    .position(user.getPosition())
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
//            Position position = positionRepository.findById(organizerRequest.getPositionId())
//                    .orElseThrow(() -> new AppException(ErrorCode.POSITION_NOT_FOUND));
            EventParticipant participant = EventParticipant.builder()
                    .event(event)
                    .user(user)
                    .organizerRole(role)
                    .position(user.getPosition())
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
        String qrCodeUrl = qrCodeService.generateAndSaveQrCodeEvent(event.getId());
        event.setQrCodeUrl(qrCodeUrl);

        // Gửi thông báo đến tất cả ADMIN khi tin tức mới được tạo
        List<User> admins = userRepository.findByRoles_Name("ADMIN");
        for (User admin : admins) {
            sendRealTimeNotification(
                    admin.getId(),
                    "Sự kiện mới đã được tạo",
                    "Sự kiện '" + event.getName() + "' đã được tạo bởi " +
                            creator.getFirstName() + " " + creator.getLastName(),
                    NotificationType.NEW_NEWS_CREATED,
                    event.getId()
            );
        }

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
                        a.getUser().getUsername(),
                        a.getUser().getFirstName(),
                        a.getUser().getLastName(),
                        a.getIsAttending(),
                        a.getCheckedInAt()
                ))
                .collect(Collectors.toList());
    }
    @Transactional
    public EventResponse addAttendee(String eventId, String userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));


        // Kiểm tra còn chỗ trống không nếu có giới hạn
        if (event.getMaxAttendees() != null &&
                event.getCurrentAttendeesCount() >= event.getMaxAttendees()) {
            throw new AppException(ErrorCode.EVENT_ATTENDEE_LIMIT_REACHED);
        }

        EventAttendee attendee = new EventAttendee();
        attendee.setEvent(event);
        attendee.setUser(user);
        attendee.setIsAttending(null); // Mặc định tham gia
        event.incrementAttendeesCount(); // Tăng số lượng người tham gia

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
        event.decrementAttendeesCount(); // Giảm số lượng người tham gia
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
            attendee.setIsAttending(isAttending); // Chỉ cập nhật trạng thái của attendee đó
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
        sendRealTimeNotification(
                event.getCreatedBy().getId(),
                "Sự kiện bị từ chối",
                "Sự kiện '" + event.getName() + "' bị từ chối. Lý do: " + reason,
                NotificationType.NEWS_REJECTED,
                eventId
        );
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
        // Gửi thông báo real-time
        sendRealTimeNotification(
                event.getCreatedBy().getId(),
                "Sự kiện đã được duyệt",
                "Sự kiện '" + event.getName() + "' của bạn đã được phê duyệt",
                NotificationType.NEWS_APPROVED,
                eventId
        );
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
//    public EventResponse updateEvent(String eventId, EventUpdateRequest request) {
//        // 1. Tìm event hiện tại
//        Event existingEvent = eventRepository.findById(eventId)
//                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));
//
//        // 2. Validate request
//        validateEventUpdateRequest(request);
//
//        // 3. Cập nhật thông tin cơ bản
//        existingEvent.setName(request.getName());
//        existingEvent.setPurpose(request.getPurpose());
//        existingEvent.setTime(request.getTime());
//        existingEvent.setLocation(request.getLocation());
//        existingEvent.setContent(request.getContent());
//
//        // 4. Xử lý organizers - không clear() mà xóa từng phần tử riêng lẻ
//        // Xóa organizers không còn trong request
//        existingEvent.getOrganizers().removeIf(existingOrg ->
//                request.getOrganizers().stream()
//                        .noneMatch(reqOrg ->
//                                reqOrg.getUserId().equals(existingOrg.getUser().getId()) &&
//                                        reqOrg.getRoleId().equals(existingOrg.getOrganizerRole().getId()) &&
//                                        reqOrg.getPositionId().equals(existingOrg.getPosition().getId())
//                        )
//        );
//
//        // Thêm organizers mới
//        for (OrganizerRequest organizerRequest : request.getOrganizers()) {
//            boolean exists = existingEvent.getOrganizers().stream()
//                    .anyMatch(org ->
//                            organizerRequest.getUserId().equals(org.getUser().getId()) &&
//                                    organizerRequest.getRoleId().equals(org.getOrganizerRole().getId()) &&
//                                    organizerRequest.getPositionId().equals(org.getPosition().getId())
//                    );
//
//            if (!exists) {
//                User user = userRepository.findById(organizerRequest.getUserId())
//                        .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
//                OrganizerRole role = organizerRoleRepository.findById(organizerRequest.getRoleId())
//                        .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
//                Position position = positionRepository.findById(organizerRequest.getPositionId())
//                        .orElseThrow(() -> new AppException(ErrorCode.POSITION_NOT_FOUND));
//
//                EventOrganizer organizer = EventOrganizer.builder()
//                        .event(existingEvent)
//                        .user(user)
//                        .organizerRole(role)
//                        .position(position)
//                        .build();
//
//                existingEvent.getOrganizers().add(organizer);
//            }
//        }
//
//        // 5. Xử lý participants tương tự organizers
//        existingEvent.getParticipants().removeIf(existingPart ->
//                request.getParticipants().stream()
//                        .noneMatch(reqPart ->
//                                reqPart.getUserId().equals(existingPart.getUser().getId()) &&
//                                        reqPart.getRoleId().equals(existingPart.getOrganizerRole().getId()) &&
//                                        reqPart.getPositionId().equals(existingPart.getPosition().getId())
//                        )
//        );
//
//        for (OrganizerRequest participantRequest : request.getParticipants()) {
//            boolean exists = existingEvent.getParticipants().stream()
//                    .anyMatch(part ->
//                            participantRequest.getUserId().equals(part.getUser().getId()) &&
//                                    participantRequest.getRoleId().equals(part.getOrganizerRole().getId()) &&
//                                    participantRequest.getPositionId().equals(part.getPosition().getId())
//                    );
//
//            if (!exists) {
//                User user = userRepository.findById(participantRequest.getUserId())
//                        .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
//                OrganizerRole role = organizerRoleRepository.findById(participantRequest.getRoleId())
//                        .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
//                Position position = positionRepository.findById(participantRequest.getPositionId())
//                        .orElseThrow(() -> new AppException(ErrorCode.POSITION_NOT_FOUND));
//
//                EventParticipant participant = EventParticipant.builder()
//                        .event(existingEvent)
//                        .user(user)
//                        .organizerRole(role)
//                        .position(position)
//                        .build();
//
//                existingEvent.getParticipants().add(participant);
//            }
//        }
//
//        // 6. Lưu lại
//        Event updatedEvent = eventRepository.save(existingEvent);
//        return eventMapper.toEventResponse(updatedEvent);
//    }

    @Transactional
    public EventResponse updateEvent(String eventId, EventUpdateRequest request, String updatedByUserId) {
        // 1. Tìm event hiện tại
        Event existingEvent = eventRepository.findById(eventId)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

        // Lấy thông tin người cập nhật
        User updatedBy = userRepository.findById(updatedByUserId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // Tạo bản sao của event trước khi cập nhật để so sánh
        Event oldEvent = new Event();
        BeanUtils.copyProperties(existingEvent, oldEvent);

        // 2. Validate request
        validateEventUpdateRequest(request);

        // Chuyển trạng thái về PENDING khi cập nhật
        existingEvent.setStatus(EventStatus.PENDING);
        existingEvent.setRejectionReason(null); // Xóa lý do từ chối cũ nếu có

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
                                reqOrg.getUserId().equals(existingOrg.getUser().getId())
                        )
        );

        // Thêm organizers mới
        for (OrganizerRequest organizerRequest : request.getOrganizers()) {
            boolean exists = existingEvent.getOrganizers().stream()
                    .anyMatch(org ->
                            organizerRequest.getUserId().equals(org.getUser().getId())
                    );

            if (!exists) {
                User user = userRepository.findById(organizerRequest.getUserId())
                        .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

                EventOrganizer organizer = EventOrganizer.builder()
                        .event(existingEvent)
                        .user(user)
                        .organizerRole(user.getOrganizerRole()) // Lấy từ user thay vì request
                        .position(user.getPosition()) // Lấy từ user thay vì request
                        .build();

                existingEvent.getOrganizers().add(organizer);

                // Thêm user vào group chat nếu chưa có
                if (existingEvent.getGroupChat() != null) {
                    existingEvent.getGroupChat().getMembers().add(user);
                }
            }
        }

        // 5. Xử lý participants tương tự organizers
        existingEvent.getParticipants().removeIf(existingPart ->
                request.getParticipants().stream()
                        .noneMatch(reqPart ->
                                reqPart.getUserId().equals(existingPart.getUser().getId())
                        )
        );

        for (OrganizerRequest participantRequest : request.getParticipants()) {
            boolean exists = existingEvent.getParticipants().stream()
                    .anyMatch(part ->
                            participantRequest.getUserId().equals(part.getUser().getId())
                    );

            if (!exists) {
                User user = userRepository.findById(participantRequest.getUserId())
                        .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
                OrganizerRole role = organizerRoleRepository.findById(participantRequest.getRoleId())
                        .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

                EventParticipant participant = EventParticipant.builder()
                        .event(existingEvent)
                        .user(user)
                        .organizerRole(role)
                        .position(user.getPosition()) // Lấy từ user thay vì request
                        .build();

                existingEvent.getParticipants().add(participant);

                // Thêm user vào group chat nếu chưa có
                if (existingEvent.getGroupChat() != null) {
                    existingEvent.getGroupChat().getMembers().add(user);
                }
            }
        }

        // 6. Cập nhật tên group chat nếu tên event thay đổi
        if (existingEvent.getGroupChat() != null &&
                !existingEvent.getGroupChat().getName().equals(existingEvent.getName())) {
            existingEvent.getGroupChat().setName(existingEvent.getName());
        }

        // Lưu lịch sử thay đổi
        saveEventChanges(oldEvent, existingEvent, updatedBy);
        // 7. Lưu lại
        Event updatedEvent = eventRepository.save(existingEvent);

        // Kiểm tra role của người cập nhật để gửi thông báo phù hợp
        boolean isAdmin = updatedBy.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ADMIN"));

        boolean isUser = updatedBy.getRoles().stream()
                .anyMatch(role -> role.getName().equals("USER"));

        if (isAdmin) {
            // Nếu người cập nhật là ADMIN, gửi thông báo cho USER tạo bài viết
            sendRealTimeNotification(
                    existingEvent.getCreatedBy().getId(),
                    "Sự kiện đã được cập nhật",
                    "Sự kiện '" + existingEvent.getName() + "' đã được cập nhật bởi quản trị viên " + updatedBy.getFirstName() + " " + updatedBy.getLastName() + "'",
                    NotificationType.NEWS_UPDATED,
                    eventId
            );
        } else if (isUser) {
            // Nếu người cập nhật là USER, gửi thông báo cho tất cả ADMIN
            List<User> admins = userRepository.findByRoles_Name("ADMIN");
            for (User admin : admins) {
                sendRealTimeNotification(
                        admin.getId(),
                        "Sự kiện đã được cập nhật bởi người dùng",
                        "Sự kiện '" + existingEvent.getName() + "' đã được cập nhật bởi người dùng " +
                                updatedBy.getFirstName() + " " + updatedBy.getLastName(),
                        NotificationType.NEWS_UPDATED_BY_USER,
                        eventId
                );
            }
        }


        return eventMapper.toEventResponse(updatedEvent);
    }

    private void saveEventChanges(Event oldEvent, Event newEvent, User updatedBy) {
        List<EventHistory> changes = new ArrayList<>();

        // Kiểm tra từng trường thay đổi
        if (!Objects.equals(oldEvent.getName(), newEvent.getName())) {
            changes.add(createHistoryRecord(newEvent, "name",
                    oldEvent.getName(), newEvent.getName(), updatedBy));
        }

        if (!Objects.equals(oldEvent.getPurpose(), newEvent.getPurpose())) {
            changes.add(createHistoryRecord(newEvent, "purpose",
                    oldEvent.getPurpose(), newEvent.getPurpose(), updatedBy));
        }

        if (!Objects.equals(oldEvent.getTime(), newEvent.getTime())) {
            changes.add(createHistoryRecord(newEvent, "time",
                    oldEvent.getTime() != null ? oldEvent.getTime().toString() : null,
                    newEvent.getTime() != null ? newEvent.getTime().toString() : null,
                    updatedBy));
        }

        if (!Objects.equals(oldEvent.getLocation(), newEvent.getLocation())) {
            changes.add(createHistoryRecord(newEvent, "location",
                    oldEvent.getLocation(), newEvent.getLocation(), updatedBy));
        }

        if (!Objects.equals(oldEvent.getContent(), newEvent.getContent())) {
            changes.add(createHistoryRecord(newEvent, "content",
                    oldEvent.getContent(), newEvent.getContent(), updatedBy));
        }

        if (!Objects.equals(oldEvent.getStatus(), newEvent.getStatus())) {
            changes.add(createHistoryRecord(newEvent, "status",
                    oldEvent.getStatus() != null ? oldEvent.getStatus().name() : null,
                    newEvent.getStatus() != null ? newEvent.getStatus().name() : null,
                    updatedBy));
        }

//        // Lưu thay đổi organizers
//        if (!Objects.equals(oldEvent.getOrganizers(), newEvent.getOrganizers())) {
//            String oldOrganizers = oldEvent.getOrganizers().stream()
//                    .map(org -> org.getUser().getId())
//                    .collect(Collectors.joining(","));
//            String newOrganizers = newEvent.getOrganizers().stream()
//                    .map(org -> org.getUser().getId())
//                    .collect(Collectors.joining(","));
//
//            changes.add(createHistoryRecord(newEvent, "organizers",
//                    oldOrganizers, newOrganizers, updatedBy));
//        }
//
//        // Lưu thay đổi participants
//        if (!Objects.equals(oldEvent.getParticipants(), newEvent.getParticipants())) {
//            String oldParticipants = oldEvent.getParticipants().stream()
//                    .map(part -> part.getUser().getId())
//                    .collect(Collectors.joining(","));
//            String newParticipants = newEvent.getParticipants().stream()
//                    .map(part -> part.getUser().getId())
//                    .collect(Collectors.joining(","));
//
//            changes.add(createHistoryRecord(newEvent, "participants",
//                    oldParticipants, newParticipants, updatedBy));
//        }


        // Xử lý organizers
        Set<String> oldOrganizerIds = oldEvent.getOrganizers().stream()
                .map(org -> org.getUser().getId())
                .collect(Collectors.toSet());

        Set<String> newOrganizerIds = newEvent.getOrganizers().stream()
                .map(org -> org.getUser().getId())
                .collect(Collectors.toSet());

        // Tìm organizers bị xóa
        Set<String> removedOrganizers = new HashSet<>(oldOrganizerIds);
        removedOrganizers.removeAll(newOrganizerIds);
        for (String userId : removedOrganizers) {
            changes.add(createHistoryRecord(newEvent, "organizer_removed",
                    userId, null, updatedBy));
        }

        // Tìm organizers được thêm
        Set<String> addedOrganizers = new HashSet<>(newOrganizerIds);
        addedOrganizers.removeAll(oldOrganizerIds);
        for (String userId : addedOrganizers) {
            changes.add(createHistoryRecord(newEvent, "organizer_added",
                    null, userId, updatedBy));
        }

        // Xử lý participants tương tự
        Set<String> oldParticipantIds = oldEvent.getParticipants().stream()
                .map(part -> part.getUser().getId())
                .collect(Collectors.toSet());

        Set<String> newParticipantIds = newEvent.getParticipants().stream()
                .map(part -> part.getUser().getId())
                .collect(Collectors.toSet());

        // Tìm participants bị xóa
        Set<String> removedParticipants = new HashSet<>(oldParticipantIds);
        removedParticipants.removeAll(newParticipantIds);
        for (String userId : removedParticipants) {
            changes.add(createHistoryRecord(newEvent, "participant_removed",
                    userId, null, updatedBy));
        }

        // Tìm participants được thêm
        Set<String> addedParticipants = new HashSet<>(newParticipantIds);
        addedParticipants.removeAll(oldParticipantIds);
        for (String userId : addedParticipants) {
            changes.add(createHistoryRecord(newEvent, "participant_added",
                    null, userId, updatedBy));
        }



        // Lưu tất cả thay đổi
        if (!changes.isEmpty()) {
            eventHistoryRepository.saveAll(changes);
            log.info("Đã lưu {} thay đổi cho sự kiện ID: {}", changes.size(), newEvent.getId());
        }
    }

    private EventHistory createHistoryRecord(Event event, String fieldName,
                                             String oldValue, String newValue, User updatedBy) {
        return EventHistory.builder()
                .event(event)
                .fieldName(fieldName)
                .oldValue(oldValue)
                .newValue(newValue)
                .updatedBy(updatedBy)
                .build();
    }

    public List<EventHistoryResponse> getEventHistory(String eventId) {
        return eventHistoryRepository.findByEventIdOrderByUpdatedAtDesc(eventId).stream()
                .map(this::toEventHistoryResponse)
                .collect(Collectors.toList());
    }

    private EventHistoryResponse toEventHistoryResponse(EventHistory history) {
        return EventHistoryResponse.builder()
                .id(history.getId())
                .fieldName(history.getFieldName())
                .oldValue(history.getOldValue())
                .newValue(history.getNewValue())
                .updatedBy(newsMapper.toUserBriefResponse(history.getUpdatedBy()))
                .updatedAt(history.getUpdatedAt())
                .build();
    }

//    public void deleteEvent(String eventId) {
//        // 1. Kiểm tra tồn tại
//        Event event = eventRepository.findById(eventId)
//                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));
//
//        // 2. Xóa tất cả quan hệ trước (nếu cần)
//        event.getOrganizers().clear();
//        event.getParticipants().clear();
//        eventRepository.save(event); // Xóa quan hệ trước khi xóa event
//
//        // 3. Xóa event
//        eventRepository.delete(event);
//    }

    public void deleteEvent(String eventId, String deletedById) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

        User deletedBy = userRepository.findById(deletedById)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        event.markAsDeleted(deletedBy);
        eventRepository.save(event);

        log.info("Đã đánh dấu xóa sự kiện {} bởi {}", eventId, deletedById);
    }

    public Page<EventResponse> getDeletedEvents(Pageable pageable) {
        Page<Event> eventPage = eventRepository.findByDeletedTrue(pageable);
        return eventPage.map(eventMapper::toEventResponse);
    }

    public EventResponse restoreEvent(String eventId) {
        Event event = eventRepository.findDeletedEventById(eventId)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

        if (!event.isDeleted()) {
            throw new AppException(ErrorCode.EVENT_NOT_DELETED);
        }

//        User restoredBy = userRepository.findById(restoredById)
//                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        event.setDeleted(false);
        event.setDeletedAt(null);
        event.setDeletedBy(null);
        event.setStatus(EventStatus.PENDING); // Hoặc status phù hợp khi restore

        Event restoredEvent = eventRepository.save(event);
//        log.info("Đã khôi phục sự kiện {} bởi {}", eventId, restoredById);

        return eventMapper.toEventResponse(restoredEvent);
    }

    @Transactional(readOnly = true)
    public List<EventResponse> getEventsByCreator(String userId) {
        List<Event> events = eventRepository.findByCreatedBy_Id(userId);
        events.forEach(event -> Hibernate.initialize(event.getPermissions()));

        return events.stream()
                .map(eventMapper::toEventResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EventResponse> getEventsByAttendee(String userId) {
        List<Event> events = eventRepository.findByAttendeeId(userId);
        initializePermissions(events);
        return events.stream()
                .map(eventMapper::toEventResponse)
                .collect(Collectors.toList());
    }

    private void initializePermissions(List<Event> events) {
        events.forEach(event -> Hibernate.initialize(event.getPermissions()));
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
        sendMemberRemovedNotification(groupChatId, memberIdToRemove);

        return convertToGroupChatResponse(groupChat);
    }
    private void sendMemberRemovedNotification(String groupChatId, String removedUserId) {
        try {
            socketIOServer.getRoomOperations(groupChatId)
                    .sendEvent("member_removed", Map.of(
                            "groupId", groupChatId,
                            "removedUserId", removedUserId,
                            "timestamp", new Date()
                    ));
        } catch (Exception e) {
//            log.error("Failed to send member removed notification", e);
        }
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
        sendMemberLeftNotification(groupChatId, memberId);


        return convertToGroupChatResponse(groupChat);
    }

    private void sendMemberLeftNotification(String groupChatId, String leftUserId) {
        try {
            socketIOServer.getRoomOperations(groupChatId)
                    .sendEvent("member_left", Map.of(
                            "groupId", groupChatId,
                            "leftUserId", leftUserId,
                            "timestamp", new Date()
                    ));
        } catch (Exception e) {
//            log.error("Failed to send member left notification", e);
        }
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

    private void sendRealTimeNotification(String userId, String title, String message,
                                          NotificationType type, String relatedId) {
        // Tạo đối tượng Notification thực sự
        Notification notificationEntity = Notification.builder()
                .user(userRepository.findById(userId).orElseThrow()) // Lấy user từ database
                .title(title)
                .content(message)
                .type(type)
                .relatedId(relatedId)
                .build();

        // Gửi socket message (giữ nguyên)
        Map<String, Object> socketMessage = new HashMap<>();
        socketMessage.put("title", title);
        socketMessage.put("message", message);
        socketMessage.put("type", type.name());
        socketMessage.put("relatedId", relatedId);
        socketMessage.put("timestamp", new Date());
        socketIOServer.getRoomOperations(userId).sendEvent("notification", socketMessage);

        // Lưu vào database
        notificationRepository.save(notificationEntity);
        log.info("Sent real-time notification to user {}", userId);
    }

    @Scheduled(cron = "0 */5 * * * *")  // Chạy vào phút thứ 0,5,10,... mỗi giờ
//    @Scheduled(cron = "*/20 * * * * *")
    @Transactional
    public void updateEventStatuses() {
        LocalDateTime now = LocalDateTime.now();
//        LocalDateTime startTime = now.minusMinutes(DEFAULT_DURATION_MINUTES);
        LocalDateTime startTime = now.minusMinutes(3000);
        LocalDateTime enddTime = now.plusMinutes(3000);
        List<Event> eventsToUpdate = eventRepository.findEventsForStatusUpdate(startTime, enddTime);

        eventsToUpdate.forEach(event -> {
            LocalDateTime endTime = event.getTime().plusMinutes(DEFAULT_DURATION_MINUTES);

            if (now.isBefore(event.getTime())) {
                event.setProgressStatus(EventProgressStatus.UPCOMING);
            }
            else if (now.isBefore(endTime)) {
                event.setProgressStatus(EventProgressStatus.ONGOING);
            }
            else {
                event.setProgressStatus(EventProgressStatus.COMPLETED);
            }
        });

        eventRepository.saveAll(eventsToUpdate);
        log.info("Đã cập nhật trạng thái cho {} sự kiện", eventsToUpdate.size());
    }

    public EventResponse updateEventAvatar(String eventId, MultipartFile file) throws IOException {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

        String avatarUrl = cloudinaryService.uploadFile(file);
        event.setAvatarUrl(avatarUrl);

        return eventMapper.toEventResponse(eventRepository.save(event));
    }

    @Transactional
    public AttendanceResponse checkInAttendee(String eventId, String qrCodeData) {
        String userId = extractUserId(qrCodeData);

        // 1. Validate event
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

        // 2. Validate event status (chỉ điểm danh khi event đang diễn ra)
        if (event.getProgressStatus() != EventProgressStatus.ONGOING) {
            throw new AppException(ErrorCode.EVENT_NOT_IN_PROGRESS);
        }


        // 4. Validate attendee from QR code
        User attendee = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // 5. Check attendance record
        EventAttendee attendance = eventAttendeeRepository.findByEventAndUser(event, attendee)
                .orElseThrow(() -> new AppException(ErrorCode.ATTENDEE_NOT_REGISTERED));

        // 6. Check if already checked in
        if (attendance.getCheckedInAt() != null) {
            throw new AppException(ErrorCode.ATTENDEE_ALREADY_CHECKED_IN);
        }

        // 7. Process check-in
        attendance.setCheckedInAt(LocalDateTime.now());
        attendance.setIsAttending(true);
        EventAttendee savedAttendance = eventAttendeeRepository.save(attendance);

        // 8. Return detailed response
        return AttendanceResponse.builder()
                .eventId(event.getId())
                .eventName(event.getName())
                .attendeeId(attendee.getId())
                .checkedInAt(savedAttendance.getCheckedInAt())
                .build();
    }
    @Transactional
    public AttendanceResponse checkInAttendeeEvent(String userId, String qrCodeData) {
        String eventId = extractUserId(qrCodeData);

        // 1. Validate event
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

        // 2. Validate event status (chỉ điểm danh khi event đang diễn ra)
        if (event.getProgressStatus() != EventProgressStatus.ONGOING) {
            throw new AppException(ErrorCode.EVENT_NOT_IN_PROGRESS);
        }


        // 4. Validate attendee from QR code
        User attendee = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // 5. Check attendance record
        EventAttendee attendance = eventAttendeeRepository.findByEventAndUser(event, attendee)
                .orElseThrow(() -> new AppException(ErrorCode.ATTENDEE_NOT_REGISTERED));

        // 6. Check if already checked in
        if (attendance.getCheckedInAt() != null) {
            throw new AppException(ErrorCode.ATTENDEE_ALREADY_CHECKED_IN);
        }

        // 7. Process check-in
        attendance.setCheckedInAt(LocalDateTime.now());
        attendance.setIsAttending(true);
        EventAttendee savedAttendance = eventAttendeeRepository.save(attendance);

        // 8. Return detailed response
        return AttendanceResponse.builder()
                .eventId(event.getId())
                .eventName(event.getName())
                .attendeeId(attendee.getId())
                .checkedInAt(savedAttendance.getCheckedInAt())
                .build();
    }
    private String extractUserId(String qrCodeData) {
        try {
            // Tách chuỗi theo ký tự "|"
            String[] parts = qrCodeData.split("\\|");

            // Lấy phần chứa USER:UUID
            String userPart = parts[0]; // "USER:f144b43a-fac2-4069-a366-33a168c79333"

            // Trả về phần sau "USER:"
            return userPart.substring(5); // "f144b43a-fac2-4069-a366-33a168c79333"
        } catch (Exception e) {
            throw new AppException(ErrorCode.INVALID_QR_FORMAT);
        }
    }
}
