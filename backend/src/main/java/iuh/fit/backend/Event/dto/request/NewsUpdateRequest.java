package iuh.fit.backend.Event.dto.request;

import jakarta.annotation.Nullable;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NewsUpdateRequest {
    @Nullable
    String title;

    @Nullable
    String content;

    @Nullable
    MultipartFile coverImage;

    @Nullable
    Boolean featured;

    @Nullable
    Boolean pinned;

    @Nullable
    String eventId;
}

