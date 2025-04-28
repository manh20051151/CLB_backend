package iuh.fit.backend.Event.controller;

import iuh.fit.backend.Event.Entity.Notification;
import iuh.fit.backend.Event.dto.response.NotificationResponse;
import iuh.fit.backend.Event.mapper.NotificationMapper;
import iuh.fit.backend.Event.repository.NotificationRepository;
import iuh.fit.backend.identity.dto.request.ApiResponse;
import iuh.fit.backend.identity.exception.AppException;
import iuh.fit.backend.identity.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    @GetMapping
    public ApiResponse<List<NotificationResponse>> getUserNotifications(
            @RequestParam String userId,
            @RequestParam(defaultValue = "5") int limit) {

        // Sử dụng phương thức findLatestNotifications với @Query
        List<Notification> notifications = notificationRepository.findLatestNotifications(userId, limit);

        List<NotificationResponse> responseList = notifications.stream()
                .map(notification -> notificationMapper.toResponse(notification))
                .collect(Collectors.toList());

        return ApiResponse.<List<NotificationResponse>>builder()
                .code(1000)
                .message("Lấy thông báo thành công")
                .result(responseList)
                .build();
    }

    @PutMapping("/{notificationId}/read")
    public ApiResponse<Void> markAsRead(@PathVariable String notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new AppException(ErrorCode.NOTIFICATION_NOT_FOUND));

        notification.setRead(true);
        notificationRepository.save(notification);

        return ApiResponse.<Void>builder()
                .code(1000)
                .message("Đã đánh dấu là đọc")
                .build();
    }
}
