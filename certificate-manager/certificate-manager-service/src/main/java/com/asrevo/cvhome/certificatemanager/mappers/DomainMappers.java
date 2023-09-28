package com.asrevo.cvhome.certificatemanager.mappers;

import com.asrevo.cvhome.certificatemanager.commons.dto.DomainCreateResponseDto;
import com.asrevo.cvhome.certificatemanager.entity.DomainEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DomainMappers {
    DomainCreateResponseDto toDomainCreateResponse(DomainEntity entity);
}
