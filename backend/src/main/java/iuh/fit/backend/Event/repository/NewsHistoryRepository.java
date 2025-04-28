package iuh.fit.backend.Event.repository;

import iuh.fit.backend.Event.Entity.NewsHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NewsHistoryRepository extends JpaRepository<NewsHistory, String> {
    List<NewsHistory> findByNewsIdOrderByUpdatedAtDesc(String newsId);
}
