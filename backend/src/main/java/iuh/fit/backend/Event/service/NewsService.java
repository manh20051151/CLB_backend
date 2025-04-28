package iuh.fit.backend.Event.service;

import iuh.fit.backend.Event.Entity.Event;
import iuh.fit.backend.Event.Entity.News;
import iuh.fit.backend.Event.Entity.NewsHistory;
import iuh.fit.backend.Event.dto.request.NewsCreateRequest;
import iuh.fit.backend.Event.dto.request.NewsUpdateRequest;
import iuh.fit.backend.Event.dto.response.NewsHistoryResponse;
import iuh.fit.backend.Event.dto.response.NewsResponse;
import iuh.fit.backend.Event.enums.NewsStatus;
import iuh.fit.backend.Event.mapper.NewsMapper;
import iuh.fit.backend.Event.repository.EventRepository;
import iuh.fit.backend.Event.repository.NewsHistoryRepository;
import iuh.fit.backend.Event.repository.NewsRepository;
import iuh.fit.backend.identity.entity.User;
import iuh.fit.backend.identity.exception.AppException;
import iuh.fit.backend.identity.exception.ErrorCode;
import iuh.fit.backend.identity.mapper.UserMapper;
import iuh.fit.backend.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NewsService {
    private final NewsHistoryRepository newsHistoryRepository;
    private final NewsRepository newsRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final NewsMapper newsMapper;
    private final CloudinaryService cloudinaryService;

    private final UserMapper userMapper;
    @Transactional
    public NewsResponse createNews(NewsCreateRequest request) throws IOException {
        // Validate và lấy thông tin người tạo
        User creator = userRepository.findById(request.getCreatedById())
                .orElseThrow(() -> {
                    log.error("Người dùng không tồn tại với ID: {}", request.getCreatedById());
                    return new AppException(ErrorCode.USER_NOT_FOUND);
                });

        // Upload ảnh cover lên Cloudinary
        String coverImageUrl = cloudinaryService.uploadFile(request.getCoverImage());
        log.info("Upload ảnh bìa thành công, URL: {}", coverImageUrl);

        // Map từ request sang entity
        News news = newsMapper.toNews(request);
        news.setCoverImageUrl(coverImageUrl);
        news.setCreatedBy(creator);

        // Xử lý liên kết event nếu có
        if (request.getEventId() != null) {
            Event event = eventRepository.findById(request.getEventId())
                    .orElseThrow(() -> {
                        log.error("Sự kiện không tồn tại với ID: {}", request.getEventId());
                        return new AppException(ErrorCode.EVENT_NOT_FOUND);
                    });
            news.setEvent(event);
        }

        // Lưu vào database
        News savedNews = newsRepository.save(news);
        log.info("Tạo tin tức thành công với ID: {}", savedNews.getId());

        return newsMapper.toNewsResponse(savedNews);
    }

    @Transactional
    public NewsResponse rejectNews(String newsId, String reason) {
        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> {
                    log.error("Không tìm thấy tin tức với ID: {}", newsId);
                    return new AppException(ErrorCode.NEWS_NOT_FOUND);
                });

        news.reject(reason); // Sử dụng method reject đã có trong entity
        news = newsRepository.save(news);

        log.info("Đã từ chối tin tức ID: {}, lý do: {}", newsId, reason);
        return newsMapper.toNewsResponse(news);
    }

    @Transactional
    public NewsResponse approveNews(String newsId) {
        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> {
                    log.error("Không tìm thấy tin tức với ID: {}", newsId);
                    return new AppException(ErrorCode.NEWS_NOT_FOUND);
                });

        news.approve(); // Sử dụng method approve đã có trong entity
        news.setPublishedAt(LocalDateTime.now()); // Cập nhật thời gian publish khi approve
        news = newsRepository.save(news);

        log.info("Đã phê duyệt tin tức ID: {}", newsId);
        return newsMapper.toNewsResponse(news);
    }


    public List<NewsResponse> getNewsByStatus(NewsStatus status) {
        List<News> newsList;

        if (status != null) {
            log.info("Lấy danh sách tin tức theo trạng thái: {}", status);
            newsList = newsRepository.findByStatus(status);
        } else {
            log.info("Lấy tất cả danh sách tin tức");
            newsList = newsRepository.findAll();
        }

        return newsList.stream()
                .map(newsMapper::toNewsResponse)
                .collect(Collectors.toList());
    }

    // Phiên bản phân trang
    public Page<NewsResponse> getNewsByStatus(NewsStatus status, Pageable pageable) {
        Page<News> newsPage;

        if (status != null) {
            log.info("Lấy danh sách tin tức theo trạng thái {} (trang {})", status, pageable.getPageNumber());
            newsPage = newsRepository.findByStatus(status, pageable);
        } else {
            log.info("Lấy tất cả danh sách tin tức (trang {})", pageable.getPageNumber());
            newsPage = newsRepository.findAll(pageable);
        }

        return newsPage.map(newsMapper::toNewsResponse);
    }

    // Phiên bản phân trang với sắp xếp tùy chỉnh
    public Page<NewsResponse> getNewsByStatusWithCustomSort(NewsStatus status, String direction, Pageable pageable) {
        Page<News> newsPage = newsRepository.findByStatusWithSort(status, direction, pageable);

        log.info("Lấy danh sách tin tức - Trạng thái: {}, Hướng: {}, Trang: {}",
                status, direction, pageable.getPageNumber());

        return newsPage.map(newsMapper::toNewsResponse);
    }


    // Phiên bản phân trang
    public Page<NewsResponse> getFeaturedApprovedNews(Pageable pageable) {
        Page<News> newsPage = newsRepository.findFeaturedApprovedNews(pageable);
        log.info("Lấy danh sách tin tức nổi bật đã duyệt, trang {}", pageable.getPageNumber());
        return newsPage.map(newsMapper::toNewsResponse);
    }

    // Phiên bản phân trang
    public Page<NewsResponse> getPinnedApprovedNews(Pageable pageable) {
        Page<News> newsPage = newsRepository.findPinnedApprovedNews(pageable);
        log.info("Lấy danh sách tin tức đã ghim, trang {}", pageable.getPageNumber());
        return newsPage.map(newsMapper::toNewsResponse);
    }

    public void deleteNews(String newsId, String deletedById) {
        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new AppException(ErrorCode.NEWS_NOT_FOUND));

        User deletedBy = userRepository.findById(deletedById)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        news.markAsDeleted(deletedBy);
        newsRepository.save(news);
        log.info("Đã đánh dấu tin tức {} là đã xóa bởi {}", newsId, deletedById);
    }

    public Page<NewsResponse> getDeletedNews(Pageable pageable) {
        // Sử dụng @Query để ghi đè @Where clause
        Page<News> newsPage = newsRepository.findByDeletedTrue(pageable);
        return newsPage.map(newsMapper::toNewsResponse);
    }

    public NewsResponse restoreNews(String newsId, String restoredById) {
        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new AppException(ErrorCode.NEWS_NOT_FOUND));

        if (!news.isDeleted()) {
            throw new AppException(ErrorCode.NEWS_NOT_DELETED);
        }

        User restoredBy = userRepository.findById(restoredById)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        news.setDeleted(false);
        news.setDeletedAt(null);
        news.setDeletedBy(null);

        News restoredNews = newsRepository.save(news);
        log.info("Đã khôi phục tin tức {} bởi {}", newsId, restoredById);

        return newsMapper.toNewsResponse(restoredNews);
    }

    @Transactional
    public NewsResponse updateNews(String newsId, NewsUpdateRequest request, String updatedById) throws IOException {
        // Lấy news hiện tại
        News existingNews = newsRepository.findById(newsId)
                .orElseThrow(() -> new AppException(ErrorCode.NEWS_NOT_FOUND));

        // Lấy thông tin người cập nhật
        User updatedBy = userRepository.findById(updatedById)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Tạo bản sao của news trước khi cập nhật để so sánh
        News oldNews = new News();
        BeanUtils.copyProperties(existingNews, oldNews);

        // Cập nhật các trường từ request
        if (request.getTitle() != null) {
            existingNews.setTitle(request.getTitle());
        }

        if (request.getContent() != null) {
            existingNews.setContent(request.getContent());
        }

        if (request.getCoverImage() != null) {
            String newCoverImageUrl = cloudinaryService.uploadFile(request.getCoverImage());
            existingNews.setCoverImageUrl(newCoverImageUrl);
        }

        if (request.getFeatured() != null) {
            existingNews.setFeatured(request.getFeatured());
        }

        if (request.getPinned() != null) {
            existingNews.setPinned(request.getPinned());
        }

        if (request.getEventId() != null) {
            Event event = eventRepository.findById(request.getEventId())
                    .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));
            existingNews.setEvent(event);
        }

        // Lưu lịch sử thay đổi
        saveNewsChanges(oldNews, existingNews, updatedBy);

        // Lưu news đã cập nhật
        News updatedNews = newsRepository.save(existingNews);
        log.info("Cập nhật tin tức thành công ID: {}", newsId);

        return newsMapper.toNewsResponse(updatedNews);
    }

    private void saveNewsChanges(News oldNews, News newNews, User updatedBy) {
        List<NewsHistory> changes = new ArrayList<>();

        // Kiểm tra từng trường thay đổi
        if (!Objects.equals(oldNews.getTitle(), newNews.getTitle())) {
            changes.add(createHistoryRecord(newNews, "title",
                    oldNews.getTitle(), newNews.getTitle(), updatedBy));
        }

        if (!Objects.equals(oldNews.getContent(), newNews.getContent())) {
            changes.add(createHistoryRecord(newNews, "content",
                    oldNews.getContent(), newNews.getContent(), updatedBy));
        }

        if (!Objects.equals(oldNews.getCoverImageUrl(), newNews.getCoverImageUrl())) {
            changes.add(createHistoryRecord(newNews, "coverImageUrl",
                    oldNews.getCoverImageUrl(), newNews.getCoverImageUrl(), updatedBy));
        }

        if (oldNews.isFeatured() != newNews.isFeatured()) {
            changes.add(createHistoryRecord(newNews, "featured",
                    String.valueOf(oldNews.isFeatured()), String.valueOf(newNews.isFeatured()), updatedBy));
        }

        if (oldNews.isPinned() != newNews.isPinned()) {
            changes.add(createHistoryRecord(newNews, "pinned",
                    String.valueOf(oldNews.isPinned()), String.valueOf(newNews.isPinned()), updatedBy));
        }

        if (!Objects.equals(oldNews.getEvent(), newNews.getEvent())) {
            String oldEventId = oldNews.getEvent() != null ? oldNews.getEvent().getId() : null;
            String newEventId = newNews.getEvent() != null ? newNews.getEvent().getId() : null;
            changes.add(createHistoryRecord(newNews, "event",
                    oldEventId, newEventId, updatedBy));
        }

        // Lưu tất cả thay đổi
        if (!changes.isEmpty()) {
            newsHistoryRepository.saveAll(changes);
            log.info("Đã lưu {} thay đổi cho tin tức ID: {}", changes.size(), newNews.getId());
        }
    }

    private NewsHistory createHistoryRecord(News news, String fieldName,
                                            String oldValue, String newValue, User updatedBy) {
        return NewsHistory.builder()
                .news(news)
                .fieldName(fieldName)
                .oldValue(oldValue)
                .newValue(newValue)
                .updatedBy(updatedBy)
                .build();
    }

    public List<NewsHistoryResponse> getNewsHistory(String newsId) {
        return newsHistoryRepository.findByNewsIdOrderByUpdatedAtDesc(newsId).stream()
                .map(this::toNewsHistoryResponse)
                .collect(Collectors.toList());
    }

    private NewsHistoryResponse toNewsHistoryResponse(NewsHistory history) {
        return NewsHistoryResponse.builder()
                .id(history.getId())
                .fieldName(history.getFieldName())
                .oldValue(history.getOldValue())
                .newValue(history.getNewValue())
                .updatedBy(newsMapper.toUserBriefResponse(history.getUpdatedBy()))
                .updatedAt(history.getUpdatedAt())
                .build();
    }
}
