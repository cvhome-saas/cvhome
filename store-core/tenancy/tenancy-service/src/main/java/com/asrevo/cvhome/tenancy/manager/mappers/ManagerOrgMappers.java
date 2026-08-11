package com.asrevo.cvhome.tenancy.manager.mappers;

import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import com.asrevo.cvhome.tenancy.commons.dto.ManagerOrgDto;
import com.asrevo.cvhome.tenancy.manager.entity.ManagerOrgEntity;

@Mapper(componentModel = "spring")
public interface ManagerOrgMappers {

    ManagerOrgDto toDto(ManagerOrgEntity entity);

    default Page<ManagerOrgDto> map(Page<ManagerOrgEntity> all) {
        return new PageImpl<>(all.stream().map(this::toDto).toList(), all.getPageable(), all.getTotalElements());
    }

}
