package iuh.fit.backend.Event.repository;

import iuh.fit.backend.Event.Entity.Event;
import iuh.fit.backend.Event.Entity.News;
import iuh.fit.backend.Event.enums.NewsStatus;
import iuh.fit.backend.Event.enums.NewsType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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
    // Phiên bản phân trang
    Page<News> findByStatus(NewsStatus status, Pageable pageable);

    // Phiên bản phân trang với sắp xếp
    @Query("SELECT n FROM News n WHERE (:status IS NULL OR n.status = :status) " +
            "ORDER BY " +
            "CASE WHEN :direction = 'ASC' THEN " +
            "  CASE WHEN n.publishedAt IS NOT NULL THEN n.publishedAt ELSE n.createdAt END " +
            "ELSE NULL END ASC, " +
            "CASE WHEN :direction = 'DESC' THEN " +
            "  CASE WHEN n.publishedAt IS NOT NULL THEN n.publishedAt ELSE n.createdAt END " +
            "ELSE NULL END DESC")
    Page<News> findByStatusWithSort(
            @Param("status") NewsStatus status,
            @Param("direction") String direction,
            Pageable pageable);

    // Lấy tin tức nổi bật đã được duyệt, sắp xếp theo publishedAt
    @Query("SELECT n FROM News n WHERE n.status = 'APPROVED' AND n.featured = true " +
            "ORDER BY n.publishedAt DESC")
    Page<News> findFeaturedApprovedNews(Pageable pageable);

    // Lấy tin tức đã ghim và được duyệt, sắp xếp theo publishedAt
    @Query("SELECT n FROM News n WHERE n.status = 'APPROVED' AND n.pinned = true " +
            "ORDER BY n.publishedAt DESC")
    Page<News> findPinnedApprovedNews(Pageable pageable);

    // Query đặc biệt để lấy tin đã xóa (ghi đè @Where clause)

    @Query(value = "SELECT * FROM news WHERE deleted = true", nativeQuery = true)
    Page<News> findByDeletedTrue(Pageable pageable);

    // Query đặc biệt để tìm kiếm cả tin đã xóa
//    @Query("SELECT n FROM News n WHERE n.id = :id AND (n.deleted = false OR :includeDeleted = true)")
//    Optional<News> findByIdIncludeDeleted(@Param("id") String id, @Param("includeDeleted") boolean includeDeleted);
    @Query(value = "SELECT * FROM News WHERE id = ?1 AND deleted = true", nativeQuery = true)
    Optional<News> findDeletedNewtById(String newsId);
}
