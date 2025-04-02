package iuh.fit.backend.Event.service;

import iuh.fit.backend.Event.Entity.OrganizerRole;
import iuh.fit.backend.Event.dto.request.OrganizerRoleRequest;
import iuh.fit.backend.Event.dto.response.OrganizerRoleResponse;
import iuh.fit.backend.Event.mapper.OrganizerRoleMapper;
import iuh.fit.backend.Event.repository.OrganizerRoleRepository;
import iuh.fit.backend.identity.exception.AppException;
import iuh.fit.backend.identity.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrganizerRoleService {
    private final OrganizerRoleRepository roleRepository;
    private final OrganizerRoleMapper roleMapper;

    public OrganizerRoleResponse create(OrganizerRoleRequest request) {
        if (roleRepository.findByName(request.getName()).isPresent()) {
            throw new AppException(ErrorCode.ROLE_ALREADY_EXISTS);
        }
        OrganizerRole role = roleMapper.toEntity(request);
        return roleMapper.toResponse(roleRepository.save(role));
    }

    public List<OrganizerRoleResponse> getAll() {
        return roleRepository.findAll().stream()
                .map(roleMapper::toResponse)
                .collect(Collectors.toList());
    }

    public OrganizerRoleResponse update(String id, OrganizerRoleRequest request) {
        OrganizerRole role = roleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
        role.setName(request.getName());
        return roleMapper.toResponse(roleRepository.save(role));
    }

    public void delete(String id) {
        if (!roleRepository.existsById(id)) {
            throw new AppException(ErrorCode.ROLE_NOT_FOUND);
        }
        roleRepository.deleteById(id);
    }
}
