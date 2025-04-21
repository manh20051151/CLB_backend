package iuh.fit.backend.Event.dto.request;

import iuh.fit.backend.Event.enums.NewsType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
public class NewsCreateRequest {
    @NotBlank
    private String title;

    @NotBlank
    private String content;

    private String eventId; // Có thể null

    @NotNull
    private NewsType type;

    private MultipartFile coverImage; // File ảnh
    private boolean featured;
    private boolean pinned;
    @NotNull(message = "ID người tạo không được để trống")
    private String createdById;
}
