package ru.practicum.core.model.mapper;

import org.mapstruct.Mapper;
import ru.practicum.core.dto.event.request.LocationBody;
import ru.practicum.core.model.Location;

@Mapper
public interface LocationMapper {

    Location toEntity(LocationBody dto);
}
