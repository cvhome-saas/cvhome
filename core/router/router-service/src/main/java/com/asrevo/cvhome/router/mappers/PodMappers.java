package com.asrevo.cvhome.router.mappers;

import com.asrevo.cvhome.router.commons.dto.PodDto;
import com.asrevo.cvhome.router.entity.PodEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PodMappers {
    PodDto toDto(PodEntity entity);

    PodEntity toEntity(PodDto dto);
}
