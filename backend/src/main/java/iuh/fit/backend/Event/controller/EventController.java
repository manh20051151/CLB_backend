package iuh.fit.backend.Event.controller;

import iuh.fit.backend.Event.Entity.Event;
import iuh.fit.backend.Event.dto.request.EventCreateRequest;
import iuh.fit.backend.Event.dto.request.EventUpdateRequest;
import iuh.fit.backend.Event.dto.response.AttendeeResponse;
import iuh.fit.backend.Event.dto.response.EventResponse;
import iuh.fit.backend.Event.enums.EventStatus;
import iuh.fit.backend.Event.repository.EventRepository;
import iuh.fit.backend.Event.service.EventExportService;
import iuh.fit.backend.Event.service.EventService;
import iuh.fit.backend.identity.dto.request.ApiResponse;
import org.springframework.core.io.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;
    private final EventExportService eventExportService;
    private final EventRepository eventRepository;

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
}
