package com.asrevo.cvhome.dcm.mappers;

import com.asrevo.cvhome.dcm.commons.dto.RegisteredDomainResponse;
import com.asrevo.cvhome.dcm.entity.DomainEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DomainMappers {
    RegisteredDomainResponse toRegisteredDomainResponse(DomainEntity entity);

    List<RegisteredDomainResponse> toRegisteredDomainResponse(List<DomainEntity> domainsList);
}
