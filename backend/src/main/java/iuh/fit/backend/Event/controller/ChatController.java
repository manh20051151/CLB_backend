//package iuh.fit.backend.Event.controller;
//
//import com.corundumstudio.socketio.SocketIOClient;
//import com.corundumstudio.socketio.SocketIOServer;
//import iuh.fit.backend.Event.Entity.ChatMessage;
//import iuh.fit.backend.Event.Entity.GroupChat;
//import iuh.fit.backend.Event.dto.request.ChatMessageDto;
//import iuh.fit.backend.Event.dto.request.ChatMessageRequest;
//import iuh.fit.backend.Event.repository.ChatMessageRepository;
//import iuh.fit.backend.Event.repository.GroupChatRepository;
//import iuh.fit.backend.identity.dto.request.ApiResponse;
//import iuh.fit.backend.identity.entity.User;
//import iuh.fit.backend.identity.exception.AppException;
//import iuh.fit.backend.identity.exception.ErrorCode;
//import iuh.fit.backend.identity.repository.UserRepository;
//import jakarta.annotation.PostConstruct;
//import org.springframework.beans.factory.annotation.Autowired;
//
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Controller
//@RequestMapping("/api/chatt")
//public class ChatController {
//
//    @Autowired
//    private SocketIOServer socketIOServer;
//
//    @Autowired
//    private GroupChatRepository groupChatRepository;
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Autowired
//    private ChatMessageRepository chatMessageRepository;
//
//    @PostConstruct
//    public void setupSocketIOListeners() {
//        // Lắng nghe sự kiện chat
//        socketIOServer.addEventListener("chat_message", ChatMessageRequest.class, (client, data, ackSender) -> {
//            String groupId = client.getHandshakeData().getSingleUrlParam("groupId");
//            handleChatMessage(groupId, data, client);
//        });
//
//        // Lắng nghe kết nối mới
//        socketIOServer.addConnectListener(client -> {
//            String groupId = client.getHandshakeData().getSingleUrlParam("groupId");
//            client.joinRoom(groupId);
//            System.out.println( "Client"+client+" connected to room: " + groupId);
//        });
//    }
//
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
//
////    @GetMapping("/{groupId}/messages")
////    public ApiResponse<List<ChatMessageDto>> getMessages(@PathVariable String groupId) {
////        GroupChat groupChat = groupChatRepository.findById(groupId)
////                .orElseThrow(() -> new AppException(ErrorCode.GROUP_CHAT_NOT_FOUND));
////
////        List<ChatMessage> messages = chatMessageRepository.findByGroupChatOrderBySentAtAsc(groupChat);
////
////        List<ChatMessageDto> dtos = messages.stream()
////                .map(this::convertToDto)
////                .collect(Collectors.toList());
////
////        return ApiResponse.<List<ChatMessageDto>>builder()
////                .code(1000)
////                .message("Lấy danh sách tin nhắn thành công")
////                .result(dtos)
////                .build();
////    }
//
////    @GetMapping("/{groupId}/messages")
////    public String getMessages(@PathVariable String groupId) {
////
////        return "okok";
////    }
//
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
//}