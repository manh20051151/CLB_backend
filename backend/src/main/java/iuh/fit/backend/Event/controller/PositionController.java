package iuh.fit.backend.Event.controller;

import iuh.fit.backend.Event.dto.request.PositionRequest;
import iuh.fit.backend.Event.dto.response.PositionResponse;
import iuh.fit.backend.Event.service.PositionService;
import iuh.fit.backend.identity.dto.request.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/positions")
@RequiredArgsConstructor
public class PositionController {
    private final PositionService positionService;

    @PostMapping
    public ApiResponse<PositionResponse> create(@RequestBody @Valid PositionRequest request) {
        return ApiResponse.<PositionResponse>builder()
                .code(1000)
                .message("Tạo vị trí thành công")
                .result(positionService.create(request))
                .build();
    }

    @GetMapping
    public ApiResponse<List<PositionResponse>> getAll() {
        return ApiResponse.<List<PositionResponse>>builder()
                .code(1000)
                .message("Lấy danh sách vị trí thành công")
                .result(positionService.getAll())
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<PositionResponse> update(@PathVariable String id, @RequestBody @Valid PositionRequest request) {
        return ApiResponse.<PositionResponse>builder()
                .code(1000)
                .message("Cập nhật vị trí thành công")
                .result(positionService.update(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        positionService.delete(id);
        return ApiResponse.<Void>builder()
                .code(1000)
                .message("Xóa vị trí thành công")
                .result(null)
                .build();
    }
}