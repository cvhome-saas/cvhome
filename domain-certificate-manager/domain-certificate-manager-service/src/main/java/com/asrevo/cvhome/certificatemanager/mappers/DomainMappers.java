package com.asrevo.cvhome.certificatemanager.mappers;

import com.asrevo.cvhome.certificatemanager.commons.dto.RegisteredDomainResponse;
import com.asrevo.cvhome.certificatemanager.entity.DomainEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DomainMappers {
    RegisteredDomainResponse toRegisteredDomainResponse(DomainEntity entity);

    List<RegisteredDomainResponse> toRegisteredDomainResponse(List<DomainEntity> domainsList);
}
