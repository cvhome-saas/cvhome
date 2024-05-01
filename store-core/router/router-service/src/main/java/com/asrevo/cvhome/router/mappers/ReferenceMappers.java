package com.asrevo.cvhome.router.mappers;

import com.asrevo.cvhome.commons.domain.Domain;
import com.asrevo.cvhome.router.commons.dto.ReferenceDto;
import com.asrevo.cvhome.router.entity.ReferenceAlisEntity;
import com.asrevo.cvhome.router.entity.ReferenceEntity;
import org.mapstruct.Mapper;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ReferenceMappers {
    ReferenceDto toDto(ReferenceEntity entity);

    default Set<Domain> to(Set<ReferenceAlisEntity> alis) {
        if (alis != null) {
            return alis.stream().map(ReferenceAlisEntity::getAlis).collect(Collectors.toSet());
        } else {
            return Set.of();
        }
    }

}
