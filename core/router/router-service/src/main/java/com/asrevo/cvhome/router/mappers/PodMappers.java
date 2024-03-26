package com.asrevo.cvhome.router.mappers;

import com.asrevo.cvhome.commons.dto.PodDto;
import com.asrevo.cvhome.router.entity.PodEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PodMappers {
    PodDto toDto(PodEntity entity);

    @Mapping(target = "new", ignore = true)
    @Mapping(target = "isNew", ignore = true)
    PodEntity toEntity(PodDto dto);
}
