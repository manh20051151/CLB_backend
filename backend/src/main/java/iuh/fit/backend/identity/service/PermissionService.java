package iuh.fit.backend.identity.service;

import iuh.fit.backend.identity.dto.request.PermissionRequest;
import iuh.fit.backend.identity.dto.response.PermissionResponse;
import iuh.fit.backend.identity.entity.Permission;
import iuh.fit.backend.identity.mapper.PermissionMapper;
import iuh.fit.backend.identity.repository.PermissionRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PermissionService {
    PermissionRepository permissionRepository;
    PermissionMapper permissionMapper;

    public PermissionResponse create(PermissionRequest request){
        Permission permission = permissionMapper.toPermission(request);
        permission = permissionRepository.save(permission);
        return permissionMapper.toPermissionResponse(permission);
    }

    public List<PermissionResponse> getAll(){
        var permissions = permissionRepository.findAll();
        return permissions.stream().map(permissionMapper::toPermissionResponse).toList();

    }

    public void delete(String permission){
        permissionRepository.deleteById(permission);
    }
}
