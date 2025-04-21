package iuh.fit.backend.Event.controller;

import iuh.fit.backend.Event.dto.request.NewsCreateRequest;
import iuh.fit.backend.Event.dto.response.NewsResponse;
import iuh.fit.backend.Event.service.NewsService;
import iuh.fit.backend.identity.dto.request.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

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
}