package iuh.fit.backend.Event.mapper;


import iuh.fit.backend.Event.Entity.Position;
import iuh.fit.backend.Event.dto.request.PositionRequest;
import iuh.fit.backend.Event.dto.response.PositionResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PositionMapper {
    Position toEntity(PositionRequest request);

    PositionResponse toResponse(Position position);
}
