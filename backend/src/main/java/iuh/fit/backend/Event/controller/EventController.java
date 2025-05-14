package iuh.fit.backend.Event.controller;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import iuh.fit.backend.Event.Entity.ChatMessage;
import iuh.fit.backend.Event.Entity.Event;
import iuh.fit.backend.Event.Entity.GroupChat;
import iuh.fit.backend.Event.dto.request.ChatMessageDto;
import iuh.fit.backend.Event.dto.request.ChatMessageRequest;
import iuh.fit.backend.Event.dto.request.EventCreateRequest;
import iuh.fit.backend.Event.dto.request.EventUpdateRequest;
import iuh.fit.backend.Event.dto.response.*;
import iuh.fit.backend.Event.enums.EventStatus;
import iuh.fit.backend.Event.repository.ChatMessageRepository;
import iuh.fit.backend.Event.repository.EventRepository;
import iuh.fit.backend.Event.repository.GroupChatRepository;
import iuh.fit.backend.Event.service.CloudinaryService;
import iuh.fit.backend.Event.service.EventExportService;
import iuh.fit.backend.Event.service.EventService;
import iuh.fit.backend.Event.service.QrCodeService;
import iuh.fit.backend.identity.dto.request.ApiResponse;
import iuh.fit.backend.identity.entity.User;
import iuh.fit.backend.identity.exception.AppException;
import iuh.fit.backend.identity.exception.ErrorCode;
import iuh.fit.backend.identity.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;
    private final EventExportService eventExportService;
    private final EventRepository eventRepository;

    private final GroupChatRepository groupChatRepository;

    private final SocketIOServer socketIOServer;

    private final UserRepository userRepository;

    private final ChatMessageRepository chatMessageRepository;

    private final CloudinaryService cloudinaryService;

    private final QrCodeService qrCodeService;
    @PostMapping
    public ApiResponse<EventResponse> createEvent(@RequestBody EventCreateRequest request) throws IOException {
        return ApiResponse.<EventResponse>builder()
                .code(1000)
                .message("Tạo sự kiện thành công")
                .result(eventService.createEvent(request))
                .build();
    }

    @GetMapping
    public ApiResponse<List<EventResponse>> getEvents() {
        return ApiResponse.<List<EventResponse>>builder()
                .code(1000)
                .message("Lấy danh sách sự kiện thành công")
                .result(eventService.getAllEvents())
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<EventResponse> getEventById(@PathVariable String id) {
        return ApiResponse.<EventResponse>builder()
                .code(1000)
                .message("Lấy thông tin sự kiện theo id thành công")
                .result(eventService.getEventById(id))
                .build();
    }
//    @GetMapping("/guest")
//    public ApiResponse<List<EventResponse>> getEventsByGuest() {
//        return ApiResponse.<List<EventResponse>>builder()
//                .code(1000)
//                .message("Lấy danh sách sự kiện thành công")
//                .result(eventService.getAllEvents())
//                .build();
//    }

    @GetMapping("/{eventId}/export")
    public ResponseEntity<Resource> exportEventToWord(@PathVariable String eventId) {
        try {
            Event event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Không tìm thấy sự kiện"));

            Resource fileResource = eventExportService.exportEventToWordFile(event);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"event_" + eventId + ".docx\"")
                    .body(fileResource);
        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Lỗi khi xuất file: " + e.getMessage());
        }
    }

    @GetMapping("/{eventId}/attendees")
    public ApiResponse<List<AttendeeResponse>> getEventAttendees(
            @PathVariable String eventId,
            @RequestParam(required = false) Boolean isAttending) {
        return ApiResponse.<List<AttendeeResponse>>builder()
                .code(1000)
                .message("Lấy danh sách attendees thành công")
                .result(eventService.getEventAttendees(eventId, isAttending))
                .build();
    }

    @PostMapping("/{eventId}/attendees")
    public ApiResponse<EventResponse> addAttendee(
            @PathVariable String eventId,
            @RequestParam String userId) {
        return ApiResponse.<EventResponse>builder()
                .code(1000)
                .message("Thêm attendee thành công")
                .result(eventService.addAttendee(eventId, userId))
                .build();
    }

    @DeleteMapping("/{eventId}/attendees/{userId}")
    public ApiResponse<EventResponse> removeAttendee(
            @PathVariable String eventId,
            @PathVariable String userId) {
        return ApiResponse.<EventResponse>builder()
                .code(1000)
                .message("Xóa attendee thành công")
                .result(eventService.removeAttendee(eventId, userId))
                .build();
    }

    @PutMapping("/{eventId}/attendees/{userId}")
    public ApiResponse<EventResponse> updateAttendeeStatus(
            @PathVariable String eventId,
            @PathVariable String userId,
            @RequestParam boolean isAttending) {
        return ApiResponse.<EventResponse>builder()
                .code(1000)
                .message("Cập nhật trạng thái attendee thành công")
                .result(eventService.updateAttendeeStatus(eventId, userId, isAttending))
                .build();
    }

    @PutMapping("/{eventId}/reject")
    public ApiResponse<EventResponse> rejectEvent(
            @PathVariable String eventId,
            @RequestParam String reason) {
        return ApiResponse.<EventResponse>builder()
                .code(1000)
                .message("Từ chối sự kiện thành công")
                .result(eventService.rejectEvent(eventId, reason))
                .build();
    }
    @PutMapping("/{eventId}/approve")
    public ApiResponse<EventResponse> approveEvent(@PathVariable String eventId) {
        return ApiResponse.<EventResponse>builder()
                .code(1000)
                .message("Phê duyệt sự kiện thành công")
                .result(eventService.approveEvent(eventId))
                .build();
    }

    @GetMapping("/status")
    public ApiResponse<List<EventResponse>> getEvents(@RequestParam(required = false) EventStatus status) {
        return ApiResponse.<List<EventResponse>>builder()
                .code(1000)
                .message("Lấy danh sách sự kiện thành công")
                .result(eventService.getEventsByStatus(status))
                .build();
    }

    @GetMapping("/status/notoken")
    public ApiResponse<List<EventResponse>> getEventsNoToken(@RequestParam(required = false) EventStatus status) {
        return ApiResponse.<List<EventResponse>>builder()
                .code(1000)
                .message("Lấy danh sách sự kiện thành công")
                .result(eventService.getEventsByStatus(status))
                .build();
    }

    @PutMapping("/{eventId}")
    public ApiResponse<EventResponse> updateEvent(
            @PathVariable String eventId,
            @RequestBody EventUpdateRequest request,
            @RequestParam String updatedByUserId) {

        return ApiResponse.<EventResponse>builder()
                .code(1000)
                .message("Cập nhật sự kiện thành công. Sự kiện đã được chuyển về trạng thái chờ phê duyệt")
                .result(eventService.updateEvent(eventId, request, updatedByUserId))
                .build();
    }

    @GetMapping("/{eventId}/history")
    public ApiResponse<List<EventHistoryResponse>> getEventHistory(
            @PathVariable String eventId) {
        List<EventHistoryResponse> history = eventService.getEventHistory(eventId);
        return ApiResponse.<List<EventHistoryResponse>>builder()
                .code(1000)
                .result(history)
                .build();
    }

//    @DeleteMapping("/{eventId}")
//    public ApiResponse<Void> deleteEvent(@PathVariable String eventId) {
//        eventService.deleteEvent(eventId);
//        return ApiResponse.<Void>builder()
//                .code(1000)
//                .message("Xóa sự kiện thành công")
//                .build();
//    }


    @DeleteMapping("/{eventId}")
//    @PreAuthorize("hasRole('ADMIN') or hasRole('EVENT_MANAGER')")
    public ApiResponse<Void> deleteEvent(
            @PathVariable String eventId,
            @RequestParam String deletedById) {

        eventService.deleteEvent(eventId, deletedById);
        return ApiResponse.<Void>builder()
                .code(1000)
                .message("Đã đánh dấu sự kiện là đã xóa")
                .build();
    }

    @GetMapping("/deleted")
//    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Page<EventResponse>> getDeletedEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.<Page<EventResponse>>builder()
                .code(1000)
                .message("Lấy danh sách sự kiện đã xóa thành công")
                .result(eventService.getDeletedEvents(pageable))
                .build();
    }

    @PutMapping("/{eventId}/restore")
//    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<EventResponse> restoreEvent(
            @PathVariable String eventId) {

        return ApiResponse.<EventResponse>builder()
                .code(1000)
                .message("Khôi phục sự kiện thành công")
                .result(eventService.restoreEvent(eventId))
                .build();
    }

    @GetMapping("/{eventId}/attendees/export")
    public ResponseEntity<Resource> exportAttendeesToExcel(@PathVariable String eventId) throws IOException {
        List<AttendeeResponse> attendees = eventService.getEventAttendees(eventId, null);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));
//        Resource file = eventExportService.exportAttendeesToExcel(attendees);
        Resource file = eventExportService.exportAttendeesToExcel(attendees, event);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"attendees_list_" + eventId + ".xlsx\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);
    }
    @GetMapping("/creator/{userId}")
    public ApiResponse<List<EventResponse>> getEventsByCreator(@PathVariable String userId) {
        return ApiResponse.<List<EventResponse>>builder()
                .code(1000)
                .message("Lấy danh sách sự kiện theo người tạo thành công")
                .result(eventService.getEventsByCreator(userId))
                .build();
    }

    @GetMapping("/attendee/{userId}")
    public ApiResponse<List<EventResponse>> getEventsByAttendee(@PathVariable String userId) {
        return ApiResponse.<List<EventResponse>>builder()
                .code(1000)
                .message("Lấy danh sách sự kiện theo người tham dự thành công")
                .result(eventService.getEventsByAttendee(userId))
                .build();
    }

    @PostConstruct
    public void setupSocketIOListeners() {
        // Lắng nghe sự kiện chat
        socketIOServer.addEventListener("chat_message", ChatMessageRequest.class, (client, data, ackSender) -> {
            String groupId = client.getHandshakeData().getSingleUrlParam("groupId");
            handleChatMessage(groupId, data, client);
        });

        // Lắng nghe sự kiện delete message
        socketIOServer.addEventListener("delete_message", String.class, (client, messageId, ackSender) -> {
            try {
                String groupId = client.getHandshakeData().getSingleUrlParam("groupId");
                String userId = client.getHandshakeData().getSingleUrlParam("userId");

                ChatMessage message = chatMessageRepository.findById(messageId)
                        .orElseThrow(() -> new RuntimeException("Message not found"));

                // Kiểm tra quyền xóa
                if (!message.getSender().getId().equals(userId)) {
                    client.sendEvent("error", "Permission denied");
                    return;
                }

                // Soft delete message
                if (message.getType() == ChatMessage.MessageType.TEXT) {
                    message.setContent("tin nhắn đã bị xóa");
                } else {
                    message.setContent("tin nhắn đã bị xóa");
                }
                message.setDeleted(true);
                chatMessageRepository.save(message);

                // Phát sự kiện đến tất cả client trong phòng
                ChatMessageDto messageDto = convertToDto(message);
                socketIOServer.getRoomOperations(groupId)
                        .sendEvent("message_deleted", messageDto);

            } catch (Exception e) {
                client.sendEvent("error", "Delete failed: " + e.getMessage());
            }
        });

        // Lắng nghe kết nối mới
        socketIOServer.addConnectListener(client -> {
            String groupId = client.getHandshakeData().getSingleUrlParam("groupId");
            client.joinRoom(groupId);
            System.out.println( "Client"+client+" connected to room: " + groupId);
        });
    }

//    private void handleChatMessage(String groupId, ChatMessageRequest request, SocketIOClient senderClient) {
//        GroupChat groupChat = groupChatRepository.findById(groupId)
//                .orElseThrow(() -> new AppException(ErrorCode.GROUP_CHAT_NOT_FOUND));
//
//        User sender = userRepository.findById(request.getSenderId())
//                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
//
//        // Lưu tin nhắn vào database
//        ChatMessage message = new ChatMessage();
//        message.setContent(request.getContent());
//        message.setSender(sender);
//        message.setGroupChat(groupChat);
//        chatMessageRepository.save(message);
//
//        // Gửi tin nhắn đến tất cả client trong phòng
//        ChatMessageDto messageDto = convertToDto(message);
//        socketIOServer.getRoomOperations(groupId)
//                .sendEvent("new_message", messageDto);
//    }

    @GetMapping("/{groupId}/messages")
    public ApiResponse<List<ChatMessageDto>> getMessages(@PathVariable String groupId) {
        GroupChat groupChat = groupChatRepository.findById(groupId)
                .orElseThrow(() -> new AppException(ErrorCode.GROUP_CHAT_NOT_FOUND));

        List<ChatMessage> messages = chatMessageRepository.findByGroupChatOrderBySentAtAsc(groupChat);

        List<ChatMessageDto> dtos = messages.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        return ApiResponse.<List<ChatMessageDto>>builder()
                .code(1000)
                .message("Lấy danh sách tin nhắn thành công")
                .result(dtos)
                .build();
    }

    // API lấy danh sách tin nhắn media (ảnh và video)
    @GetMapping("/{groupId}/messages/media")
    public ApiResponse<List<ChatMessageDto>> getMediaMessages(
            @PathVariable String groupId) {

        GroupChat groupChat = groupChatRepository.findById(groupId)
                .orElseThrow(() -> new AppException(ErrorCode.GROUP_CHAT_NOT_FOUND));

        // Danh sách loại tin nhắn media (ảnh và video)
        List<ChatMessage.MessageType> mediaTypes = Arrays.asList(
                ChatMessage.MessageType.IMAGE,
                ChatMessage.MessageType.VIDEO
        );

        List<ChatMessage> messages = chatMessageRepository
                .findByGroupChatAndTypeInOrderBySentAtAsc(groupChat, mediaTypes);

        List<ChatMessageDto> dtos = messages.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        return ApiResponse.<List<ChatMessageDto>>builder()
                .code(1000)
                .message("Lấy danh sách tin nhắn media thành công")
                .result(dtos)
                .build();
    }

    // API lấy danh sách tin nhắn file
    @GetMapping("/{groupId}/messages/files")
    public ApiResponse<List<ChatMessageDto>> getFileMessages(
            @PathVariable String groupId) {

        GroupChat groupChat = groupChatRepository.findById(groupId)
                .orElseThrow(() -> new AppException(ErrorCode.GROUP_CHAT_NOT_FOUND));

        List<ChatMessage> messages = chatMessageRepository
                .findByGroupChatAndTypeOrderBySentAtAsc(
                        groupChat,
                        ChatMessage.MessageType.FILE
                );

        List<ChatMessageDto> dtos = messages.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        return ApiResponse.<List<ChatMessageDto>>builder()
                .code(1000)
                .message("Lấy danh sách tin nhắn file thành công")
                .result(dtos)
                .build();
    }

    // API lấy danh sách tin nhắn audio
    @GetMapping("/{groupId}/messages/audios")
    public ApiResponse<List<ChatMessageDto>> getAudioMessages(
            @PathVariable String groupId) {

        GroupChat groupChat = groupChatRepository.findById(groupId)
                .orElseThrow(() -> new AppException(ErrorCode.GROUP_CHAT_NOT_FOUND));

        List<ChatMessage> messages = chatMessageRepository
                .findByGroupChatAndTypeOrderBySentAtAsc(
                        groupChat,
                        ChatMessage.MessageType.AUDIO
                );

        List<ChatMessageDto> dtos = messages.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        return ApiResponse.<List<ChatMessageDto>>builder()
                .code(1000)
                .message("Lấy danh sách tin nhắn audio thành công")
                .result(dtos)
                .build();
    }

//    @PostMapping("/{groupId}/messages")
//    public ApiResponse<ChatMessageDto> sendChatMessage(
//            @PathVariable String groupId,
//            @RequestBody ChatMessageRequest request) {
//
//        GroupChat groupChat = groupChatRepository.findById(groupId)
//                .orElseThrow(() -> new AppException(ErrorCode.GROUP_CHAT_NOT_FOUND));
//
//        User sender = userRepository.findById(request.getSenderId())
//                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
//
//        // Lưu tin nhắn vào database
//        ChatMessage message = new ChatMessage();
//        message.setContent(request.getContent());
//        message.setSender(sender);
//        message.setGroupChat(groupChat);
//        message = chatMessageRepository.save(message);
//
//        // Gửi tin nhắn real-time qua Socket.IO
//        ChatMessageDto messageDto = convertToDto(message);
//        socketIOServer.getRoomOperations(groupId)
//                .sendEvent("new_message", messageDto);
//
//        return ApiResponse.<ChatMessageDto>builder()
//                .code(1000)
//                .message("Gửi tin nhắn thành công")
//                .result(messageDto)
//                .build();
//    }
//
//    private ChatMessageDto convertToDto(ChatMessage message) {
//        return ChatMessageDto.builder()
//                .id(message.getId())
//                .content(message.getContent())
//                .senderId(message.getSender().getId())
//                .senderName(message.getSender().getFirstName())
//                .sentAt(message.getSentAt())
//                .build();
//    }

    @PostMapping(value = "/{groupId}/messages", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ChatMessageDto> sendChatMessage(
            @PathVariable String groupId,
            @RequestPart(required = false) String content,
            @RequestPart(required = false) MultipartFile file,
            @RequestPart String senderId) throws IOException {

        GroupChat groupChat = groupChatRepository.findById(groupId)
                .orElseThrow(() -> new AppException(ErrorCode.GROUP_CHAT_NOT_FOUND));

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        ChatMessage message = new ChatMessage();
        message.setSender(sender);
        message.setGroupChat(groupChat);

        if (file != null && !file.isEmpty()) {
            // Upload file lên Cloudinary
            String fileUrl = cloudinaryService.uploadFile(file);

            // Xác định loại tin nhắn
            ChatMessage.MessageType type = determineMessageType(file.getContentType());

            message.setType(type);
            message.setFileUrl(fileUrl);
            message.setFileName(file.getOriginalFilename());
            message.setFileType(file.getContentType());
            message.setFileSize(file.getSize());

            // Nếu là ảnh, có thể lưu thumbnail URL
            if (type == ChatMessage.MessageType.IMAGE) {
                // Có thể thêm xử lý tạo thumbnail nếu cần
            }
        } else {
            message.setType(ChatMessage.MessageType.TEXT);
            message.setContent(content);
        }

        message = chatMessageRepository.save(message);

        // Gửi tin nhắn real-time
        ChatMessageDto messageDto = convertToDto(message);
        socketIOServer.getRoomOperations(groupId)
                .sendEvent("new_message", messageDto);

        return ApiResponse.<ChatMessageDto>builder()
                .code(1000)
                .message("Gửi tin nhắn thành công")
                .result(messageDto)
                .build();
    }
    private ChatMessage.MessageType determineMessageType(String mimeType) {
        if (mimeType == null) return ChatMessage.MessageType.FILE;

        if (mimeType.startsWith("image/")) {
            return ChatMessage.MessageType.IMAGE;
        } else if (mimeType.startsWith("video/")) {
            return ChatMessage.MessageType.VIDEO;
        } else if (mimeType.startsWith("audio/")) {
            return ChatMessage.MessageType.AUDIO;
        }
        return ChatMessage.MessageType.FILE;
    }

    // Cập nhật phương thức convertToDto
    private ChatMessageDto convertToDto(ChatMessage message) {
        return ChatMessageDto.builder()
                .id(message.getId())
                .content(message.getContent())
                .senderId(message.getSender().getId())
                .senderName(message.getSender().getFirstName())
                .sentAt(message.getSentAt())
                .type(message.getType())
                .fileUrl(message.getFileUrl())
                .fileName(message.getFileName())
                .fileType(message.getFileType())
                .fileSize(message.getFileSize())
                .deleted(message.isDeleted())
                .build();
    }


    @GetMapping("/messages/{messageId}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable String messageId) throws IOException {
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new AppException(ErrorCode.MESSAGE_NOT_FOUND));

        // Chỉ cho phép tải nếu không phải là TEXT và có fileUrl
        if (message.getType() == ChatMessage.MessageType.TEXT || message.getFileUrl() == null) {
            throw new AppException(ErrorCode.FILE_NOT_FOUND);
        }

        // Tạo URL tải file từ Cloudinary
        String downloadUrl = message.getFileUrl();

        // Tạo request để tải file từ Cloudinary
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<byte[]> response = restTemplate.getForEntity(downloadUrl, byte[].class);

        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            throw new AppException(ErrorCode.FILE_DOWNLOAD_ERROR);
        }

        // Tạo Resource từ dữ liệu tải về
        ByteArrayResource resource = new ByteArrayResource(response.getBody());

        // Thiết lập headers cho response
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(message.getFileType()));
        headers.setContentDispositionFormData("attachment", message.getFileName());
        headers.setContentLength(message.getFileSize());

        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
    }

    // Cập nhật Socket.IO handler
    private void handleChatMessage(String groupId, ChatMessageRequest request, SocketIOClient senderClient) {
        try {
            GroupChat groupChat = groupChatRepository.findById(groupId)
                    .orElseThrow(() -> new AppException(ErrorCode.GROUP_CHAT_NOT_FOUND));

            User sender = userRepository.findById(request.getSenderId())
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

            ChatMessage message = new ChatMessage();
            message.setSender(sender);
            message.setGroupChat(groupChat);

            if (request.getFile() != null && !request.getFile().isEmpty()) {
                String fileUrl = cloudinaryService.uploadFile(request.getFile());
                ChatMessage.MessageType type = determineMessageType(request.getFile().getContentType());

                message.setType(type);
                message.setFileUrl(fileUrl);
                message.setFileName(request.getFile().getOriginalFilename());
                message.setFileType(request.getFile().getContentType());
                message.setFileSize(request.getFile().getSize());
            } else {
                message.setType(ChatMessage.MessageType.TEXT);
                message.setContent(request.getContent());
            }

            message = chatMessageRepository.save(message);

            ChatMessageDto messageDto = convertToDto(message);
            socketIOServer.getRoomOperations(groupId)
                    .sendEvent("new_message", messageDto);
        } catch (IOException e) {
            senderClient.sendEvent("error", "Failed to upload file");
        }
    }

    @DeleteMapping("/messages/{messageId}")
    public ApiResponse<Void> deleteMessage(
            @PathVariable String messageId,
            @RequestParam String userId) {

        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new AppException(ErrorCode.MESSAGE_NOT_FOUND));

        if (!message.getSender().getId().equals(userId)) {
            throw new AppException(ErrorCode.PERMISSION_DENIED);
        }

        // Xử lý khác nhau cho text và file
        if (message.getType() == ChatMessage.MessageType.TEXT) {
            message.setContent("Tin nhắn đã bị xóa");
        } else {
//            message.setContent(null);
            message.setContent("Tin nhắn đã bị xóa"); // Tuỳ chỉnh thông báo
            // Giữ nguyên các trường fileUrl, fileName,... để có thể hiển thị thông tin "Đã từng gửi file"
        }

        message.setDeleted(true);
        chatMessageRepository.save(message);

        // Gửi event qua WebSocket
        ChatMessageDto messageDto = convertToDto(message);
        socketIOServer.getRoomOperations(message.getGroupChat().getId())
                .sendEvent("message_deleted", messageDto);

        return ApiResponse.<Void>builder()
                .code(1000)
                .message("Xóa tin nhắn thành công")
                .build();
    }

    @GetMapping("/group-chats/user/{userId}")
    public ApiResponse<List<GroupChatResponse>> getUserGroupChats(
            @PathVariable String userId) {
        return ApiResponse.<List<GroupChatResponse>>builder()
                .code(1000)
                .message("Lấy danh sách group thành công")
                .result(eventService.getApprovedGroupChatsByUser(userId))
                .build();
    }

    @GetMapping("/group-chats/{groupId}")
    public ApiResponse<GroupChatResponse> getGroupChatDetails(
            @PathVariable("groupId") String groupChatId) {
        return ApiResponse.<GroupChatResponse>builder()
                .code(1000)
                .message("Lấy chi tiết group thành công")
                .result(eventService.getGroupChatById(groupChatId))
                .build();
    }

    @DeleteMapping("/group-chats/{groupId}/members/{memberId}")
    public ApiResponse<GroupChatResponse> removeGroupMember(
            @PathVariable String groupId,
            @PathVariable String memberId,
            @RequestParam String leaderId) {

        return ApiResponse.<GroupChatResponse>builder()
                .code(1000)
                .message("Xóa thành viên thành công")
                .result(eventService.removeMemberFromGroupChat(groupId, leaderId, memberId))
                .build();
    }

    @PostMapping("/group-chats/{groupId}/leave")
    public ApiResponse<GroupChatResponse> leaveGroupChat(
            @PathVariable String groupId,
            @RequestParam String memberId) {

        return ApiResponse.<GroupChatResponse>builder()
                .code(1000)
                .message("Rời nhóm thành công")
                .result(eventService.leaveGroupChat(groupId, memberId))
                .build();
    }

    @PatchMapping("/group-chats/{groupId}/deactivate")
    public ApiResponse<GroupChatResponse> deactivateGroupChat(
            @PathVariable String groupId,
            @RequestParam String leaderId) {

        return ApiResponse.<GroupChatResponse>builder()
                .code(1000)
                .message("Nhóm đã chuyển trạng thái Đang chờ xử lý thành công")
                .result(eventService.deactivateGroup(groupId, leaderId))
                .build();
    }
    @PatchMapping("/{eventId}/avatar")
    public ApiResponse<EventResponse> updateAvatar(
            @PathVariable String eventId,
            @RequestPart("file") MultipartFile file) throws IOException {

        return ApiResponse.<EventResponse>builder()
                .result(eventService.updateEventAvatar(eventId, file))
                .build();
    }

    @PostMapping(value = "/{eventId}/check-in", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<AttendanceResponse> checkInAttendee(
            @PathVariable String eventId,
            @RequestParam("qrCodeData") String qrCodeData) {

        AttendanceResponse response = eventService.checkInAttendee(eventId, qrCodeData);
        return ApiResponse.<AttendanceResponse>builder()
                .code(1000)
                .message("Điểm danh thành công")
                .result(response)
                .build();
    }

    @PostMapping(value = "/{userId}/check-in-2", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<AttendanceResponse> checkInAttendeeEvent(
            @PathVariable String userId,
            @RequestParam("qrCodeData") String qrCodeData) {

        AttendanceResponse response = eventService.checkInAttendeeEvent(userId, qrCodeData);
        return ApiResponse.<AttendanceResponse>builder()
                .code(1000)
                .message("Điểm danh thành công")
                .result(response)
                .build();
    }

    @GetMapping("/{eventId}/qr-code")
    public ApiResponse<String> getUserQrCode(@PathVariable String eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

        if (event.getQrCodeUrl() == null) {
            throw new AppException(ErrorCode.QR_CODE_NOT_GENERATED);
        }

        return ApiResponse.<String>builder()
                .code(1000)
                .message("Lấy QR code thành công")
                .result(event.getQrCodeUrl())
                .build();
    }

    // Hoặc nếu muốn trả về ảnh trực tiếp
    @GetMapping("/{eventId}/qr-code-image")
    public ResponseEntity<byte[]> getQrCodeImage(@PathVariable String eventId) throws IOException, InterruptedException {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));

        if (event.getQrCodeUrl() == null) {
            throw new AppException(ErrorCode.QR_CODE_NOT_GENERATED);
        }

        byte[] imageBytes = downloadQrCodeImage(event.getQrCodeUrl());

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"qr-code.png\"")
                .body(imageBytes);
    }

    @PostMapping("/{eventId}/regenerate-qrcode")
    public ApiResponse<String> regenerateQrCode(@PathVariable String eventId) {
        try {
            String newQrCodeUrl = qrCodeService.regenerateQrCodeEvent(eventId);
            return ApiResponse.<String>builder()
                    .code(1000)
                    .message("Tạo lại QR code thành công")
                    .result(newQrCodeUrl)
                    .build();
        } catch (Exception e) {
            throw new AppException(ErrorCode.QR_CODE_GENERATION_FAILED);
        }
    }

    private byte[] downloadQrCodeImage(String qrCodeUrl) throws IOException, InterruptedException {
        // Sử dụng Java HttpClient để tải ảnh từ URL
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(qrCodeUrl))
                .build();

        HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

        if (response.statusCode() != 200) {
            throw new IOException("Không thể tải QR code từ URL: " + qrCodeUrl);
        }

        return response.body();
    }
}
