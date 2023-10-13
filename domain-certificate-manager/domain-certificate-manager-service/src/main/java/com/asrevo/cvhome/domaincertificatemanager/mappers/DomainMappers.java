package com.asrevo.cvhome.domaincertificatemanager.mappers;

import com.asrevo.cvhome.domaincertificatemanager.commons.dto.RegisteredDomainResponse;
import com.asrevo.cvhome.domaincertificatemanager.entity.DomainEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DomainMappers {
    RegisteredDomainResponse toRegisteredDomainResponse(DomainEntity entity);

    List<RegisteredDomainResponse> toRegisteredDomainResponse(List<DomainEntity> domainsList);
}
