package iuh.fit.backend.Event.controller;

import iuh.fit.backend.Event.dto.request.NewsCreateRequest;
import iuh.fit.backend.Event.dto.request.NewsUpdateRequest;
import iuh.fit.backend.Event.dto.response.NewsHistoryResponse;
import iuh.fit.backend.Event.dto.response.NewsResponse;
import iuh.fit.backend.Event.enums.NewsStatus;
import iuh.fit.backend.Event.service.NewsService;
import iuh.fit.backend.identity.dto.request.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
@Slf4j
public class NewsController {
    private final NewsService newsService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<NewsResponse> createNews(@Valid @ModelAttribute NewsCreateRequest request) throws IOException {
        log.info("Nhận yêu cầu tạo tin tức mới với tiêu đề: {}", request.getTitle());
        NewsResponse response = newsService.createNews(request);

        return ApiResponse.<NewsResponse>builder()
                .code(1000)
                .message("Tạo tin tức thành công")
                .result(response)
                .build();
    }

    @PutMapping("/{newsId}/reject")
    public ApiResponse<NewsResponse> rejectNews(
            @PathVariable String newsId,
            @RequestParam String reason) {
        log.info("Nhận yêu cầu từ chối tin tức ID: {}, lý do: {}", newsId, reason);
        return ApiResponse.<NewsResponse>builder()
                .code(1000)
                .message("Từ chối tin tức thành công")
                .result(newsService.rejectNews(newsId, reason))
                .build();
    }

    @PutMapping("/{newsId}/approve")
    public ApiResponse<NewsResponse> approveNews(@PathVariable String newsId) {
        log.info("Nhận yêu cầu phê duyệt tin tức ID: {}", newsId);
        return ApiResponse.<NewsResponse>builder()
                .code(1000)
                .message("Phê duyệt tin tức thành công")
                .result(newsService.approveNews(newsId))
                .build();
    }

    @GetMapping("/status")
    public ApiResponse<List<NewsResponse>> getNewsByStatus(
            @RequestParam(required = false) NewsStatus status) {
        return ApiResponse.<List<NewsResponse>>builder()
                .code(1000)
                .message("Lấy danh sách tin tức thành công")
                .result(newsService.getNewsByStatus(status))
                .build();
    }

    // Phiên bản phân trang
    @GetMapping("/status/paged")
    public ApiResponse<Page<NewsResponse>> getNewsByStatusPaged(
            @RequestParam(required = false) NewsStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.<Page<NewsResponse>>builder()
                .code(1000)
                .message("Lấy danh sách tin tức thành công")
                .result(newsService.getNewsByStatus(status, pageable))
                .build();
    }
    // Phiên bản phân trang với sắp xếp tùy chỉnh
    @GetMapping("/sorted/status/paged")
    public ApiResponse<Page<NewsResponse>> getNewsByStatusSorted(
            @RequestParam(required = false) NewsStatus status,
            @RequestParam(defaultValue = "DESC") String direction,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.<Page<NewsResponse>>builder()
                .code(1000)
                .message("Lấy danh sách tin tức (sắp xếp) thành công")
                .result(newsService.getNewsByStatusWithCustomSort(status, direction, pageable))
                .build();
    }

    // Phiên bản phân trang
    @GetMapping("/featured")
    public ApiResponse<Page<NewsResponse>> getFeaturedNews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("publishedAt").descending());
        return ApiResponse.<Page<NewsResponse>>builder()
                .code(1000)
                .message("Lấy danh sách tin tức nổi bật thành công")
                .result(newsService.getFeaturedApprovedNews(pageable))
                .build();
    }

    // Phiên bản phân trang
    @GetMapping("/pinned")
    public ApiResponse<Page<NewsResponse>> getPinnedNews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("publishedAt").descending());
        return ApiResponse.<Page<NewsResponse>>builder()
                .code(1000)
                .message("Lấy danh sách tin tức đã ghim thành công")
                .result(newsService.getPinnedApprovedNews(pageable))
                .build();
    }

    @DeleteMapping("/{newsId}")
//    @PreAuthorize("hasRole('ADMIN') or hasRole('EDITOR')")
    public ApiResponse<Void> deleteNews(
            @PathVariable String newsId,
            @RequestParam String deletedById) {

        newsService.deleteNews(newsId, deletedById);
        return ApiResponse.<Void>builder()
                .code(1000)
                .message("Đã đánh dấu tin tức là đã xóa")
                .build();
    }
    @GetMapping("/deleted")
//    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Page<NewsResponse>> getDeletedNews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.<Page<NewsResponse>>builder()
                .code(1000)
                .message("Lấy danh sách tin tức đã xóa thành công")
                .result(newsService.getDeletedNews(pageable))
                .build();
    }

    @PutMapping("/{newsId}/restore")
//    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<NewsResponse> restoreNews(
            @PathVariable String newsId) {

        return ApiResponse.<NewsResponse>builder()
                .code(1000)
                .message("Khôi phục tin tức thành công")
                .result(newsService.restoreNews(newsId))
                .build();
    }
    @PutMapping(value = "/{newsId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<NewsResponse> updateNews(
            @PathVariable String newsId,
            @Valid @ModelAttribute NewsUpdateRequest request,
            @RequestParam("UserId") String updatedBy) throws IOException {

        log.info("Nhận yêu cầu cập nhật tin tức ID: {}", newsId);
        NewsResponse response = newsService.updateNews(newsId, request, updatedBy);

        return ApiResponse.<NewsResponse>builder()
                .code(1000)
                .message("Cập nhật tin tức thành công")
                .result(response)
                .build();
    }

    @GetMapping("/{newsId}/history")
    public ApiResponse<List<NewsHistoryResponse>> getNewsHistory(@PathVariable String newsId) {
        return ApiResponse.<List<NewsHistoryResponse>>builder()
                .code(1000)
                .message("Lấy lịch sử thay đổi thành công")
                .result(newsService.getNewsHistory(newsId))
                .build();
    }
}