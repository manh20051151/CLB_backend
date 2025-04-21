package iuh.fit.backend.Event.service;

import iuh.fit.backend.Event.Entity.Event;
import iuh.fit.backend.Event.Entity.News;
import iuh.fit.backend.Event.dto.request.NewsCreateRequest;
import iuh.fit.backend.Event.dto.response.NewsResponse;
import iuh.fit.backend.Event.mapper.NewsMapper;
import iuh.fit.backend.Event.repository.EventRepository;
import iuh.fit.backend.Event.repository.NewsRepository;
import iuh.fit.backend.identity.entity.User;
import iuh.fit.backend.identity.exception.AppException;
import iuh.fit.backend.identity.exception.ErrorCode;
import iuh.fit.backend.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class NewsService {
    private final NewsRepository newsRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final NewsMapper newsMapper;
    private final CloudinaryService cloudinaryService;

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
}
