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
import iuh.fit.backend.Event.dto.response.AttendeeResponse;
import iuh.fit.backend.Event.dto.response.EventResponse;
import iuh.fit.backend.Event.dto.response.GroupChatResponse;
import iuh.fit.backend.Event.enums.EventStatus;
import iuh.fit.backend.Event.repository.ChatMessageRepository;
import iuh.fit.backend.Event.repository.EventRepository;
import iuh.fit.backend.Event.repository.GroupChatRepository;
import iuh.fit.backend.Event.service.CloudinaryService;
import iuh.fit.backend.Event.service.EventExportService;
import iuh.fit.backend.Event.service.EventService;
import iuh.fit.backend.identity.dto.request.ApiResponse;
import iuh.fit.backend.identity.entity.User;
import iuh.fit.backend.identity.exception.AppException;
import iuh.fit.backend.identity.exception.ErrorCode;
import iuh.fit.backend.identity.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
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
    @PostMapping
    public ApiResponse<EventResponse> createEvent(@RequestBody EventCreateRequest request) {
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

    @GetMapping("/{eventId}/export")
    public ResponseEntity<Resource> exportEventToWord(@PathVariable String eventId) {
        try {
            Event event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Không tìm thấy sự kiện"));

            Resource fileResource = (Resource) eventExportService.exportEventToWordFile(event);

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

    @PutMapping("/{eventId}")
    public ApiResponse<EventResponse> updateEvent(
            @PathVariable String eventId,
            @RequestBody EventUpdateRequest request) {
        return ApiResponse.<EventResponse>builder()
                .code(1000)
                .message("Cập nhật sự kiện thành công")
                .result(eventService.updateEvent(eventId, request))
                .build();
    }

    @DeleteMapping("/{eventId}")
    public ApiResponse<Void> deleteEvent(@PathVariable String eventId) {
        eventService.deleteEvent(eventId);
        return ApiResponse.<Void>builder()
                .code(1000)
                .message("Xóa sự kiện thành công")
                .build();
    }
    @GetMapping("/{eventId}/attendees/export")
    public ResponseEntity<Resource> exportAttendeesToExcel(@PathVariable String eventId) throws IOException {
        List<AttendeeResponse> attendees = eventService.getEventAttendees(eventId, null);
        Resource file = eventExportService.exportAttendeesToExcel(attendees);

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
            message.setContent("tin nhắn đã bị xóa");
        } else {
//            message.setContent(null);
            message.setContent("tin nhắn đã bị xóa"); // Tuỳ chỉnh thông báo
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
}
