package com.asrevo.cvhome.certificatemanager.mappers;

import com.asrevo.cvhome.certificatemanager.entity.DomainEntity;
import com.asrevo.cvhome.commons.dto.DomainCreateResponseDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DomainMappers {
    DomainCreateResponseDto toDomainCreateResponse(DomainEntity entity);
}
