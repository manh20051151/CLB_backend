package iuh.fit.backend.identity.controller;

import iuh.fit.backend.identity.dto.request.ApiResponse;
import iuh.fit.backend.identity.dto.request.PermissionRequest;
import iuh.fit.backend.identity.dto.request.RoleRequest;
import iuh.fit.backend.identity.dto.response.PermissionResponse;
import iuh.fit.backend.identity.dto.response.RoleResponse;
import iuh.fit.backend.identity.service.PermissionService;
import iuh.fit.backend.identity.service.RoleService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleController {
    RoleService roleService;

    @PostMapping
    ApiResponse<RoleResponse> create(@RequestBody RoleRequest request){
        return ApiResponse.<RoleResponse>builder()
                .result(roleService.create(request))
                .build();
    }

    @GetMapping
    ApiResponse<List<RoleResponse>> getAll(){
        return ApiResponse.<List<RoleResponse>>builder()
                .result(roleService.getAll())
                .build();
    }
    @DeleteMapping("/{role}")
    ApiResponse<Void> delete(@PathVariable String role){
        roleService.delete(role);
        return ApiResponse.<Void>builder()
                .build();
    }
}
