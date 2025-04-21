package iuh.fit.backend.Event.repository;

import iuh.fit.backend.Event.Entity.News;
import iuh.fit.backend.Event.enums.NewsStatus;
import iuh.fit.backend.Event.enums.NewsType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NewsRepository extends JpaRepository<News, String> {

    // Tìm tin tức theo event
    List<News> findByEventId(String eventId);

    // Tìm tin tức theo loại (NEWS/RECAP)
    List<News> findByType(NewsType type);

    // Tìm tin tức theo trạng thái
    List<News> findByStatus(NewsStatus status);

    // Tìm tin tức nổi bật
    List<News> findByFeaturedTrueOrderByPublishedAtDesc();

    // Tìm tin tức đã được ghim
    List<News> findByPinnedTrueOrderByPublishedAtDesc();
}
