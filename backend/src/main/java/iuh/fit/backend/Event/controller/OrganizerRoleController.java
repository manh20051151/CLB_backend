package iuh.fit.backend.Event.controller;

import iuh.fit.backend.Event.dto.request.OrganizerRoleRequest;
import iuh.fit.backend.Event.dto.response.OrganizerRoleResponse;
import iuh.fit.backend.Event.service.OrganizerRoleService;
import iuh.fit.backend.identity.dto.request.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/organizerrole")
@RequiredArgsConstructor
public class OrganizerRoleController {
    private final OrganizerRoleService roleService;

    @PostMapping
    public ApiResponse<OrganizerRoleResponse> create(@RequestBody @Valid OrganizerRoleRequest request) {
        return ApiResponse.<OrganizerRoleResponse>builder()
                .code(1000)
                .message("Tạo vai trò tổ chức thành công")
                .result(roleService.create(request))
                .build();
    }

    @GetMapping
    public ApiResponse<List<OrganizerRoleResponse>> getAll() {
        return ApiResponse.<List<OrganizerRoleResponse>>builder()
                .code(1000)
                .message("Lấy danh sách vai trò tổ chức thành công")
                .result(roleService.getAll())
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<OrganizerRoleResponse> update(@PathVariable String id, @RequestBody @Valid OrganizerRoleRequest request) {
        return ApiResponse.<OrganizerRoleResponse>builder()
                .code(1000)
                .message("Cập nhật vai trò tổ chức thành công")
                .result(roleService.update(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        roleService.delete(id);
        return ApiResponse.<Void>builder()
                .code(1000)
                .message("Xóa vai trò tổ chức thành công")
                .result(null)
                .build();
    }
}

