package iuh.fit.backend.Event.service;

import iuh.fit.backend.Event.Entity.Position;
import iuh.fit.backend.Event.dto.request.PositionRequest;
import iuh.fit.backend.Event.dto.response.PositionResponse;
import iuh.fit.backend.Event.mapper.PositionMapper;
import iuh.fit.backend.Event.repository.PositionRepository;
import iuh.fit.backend.identity.exception.AppException;
import iuh.fit.backend.identity.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PositionService {
    private final PositionRepository positionRepository;
    private final PositionMapper positionMapper;

    public PositionResponse create(PositionRequest request) {
        if (positionRepository.findByName(request.getName()).isPresent()) {
            throw new AppException(ErrorCode.POSITION_ALREADY_EXISTS);
        }
        Position position = positionMapper.toEntity(request);
        return positionMapper.toResponse(positionRepository.save(position));
    }

    public List<PositionResponse> getAll() {
        return positionRepository.findAll().stream()
                .map(positionMapper::toResponse)
                .collect(Collectors.toList());
    }

    public PositionResponse update(String id, PositionRequest request) {
        Position position = positionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.POSITION_NOT_FOUND));
        position.setName(request.getName());
        return positionMapper.toResponse(positionRepository.save(position));
    }

    public void delete(String id) {
        if (!positionRepository.existsById(id)) {
            throw new AppException(ErrorCode.POSITION_NOT_FOUND);
        }
        positionRepository.deleteById(id);
    }
}
