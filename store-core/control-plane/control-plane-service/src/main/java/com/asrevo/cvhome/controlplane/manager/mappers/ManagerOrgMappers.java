package com.asrevo.cvhome.controlplane.manager.mappers;

import com.asrevo.cvhome.controlplane.manager.commons.dto.ManagerOrgDto;
import com.asrevo.cvhome.controlplane.manager.entity.ManagerOrgEntity;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

@Mapper(componentModel = "spring")
public interface ManagerOrgMappers {

	ManagerOrgDto toDto(ManagerOrgEntity entity);

	default Page<ManagerOrgDto> map(Page<ManagerOrgEntity> all) {
		return new PageImpl<>(all.stream().map(this::toDto).toList(), all.getPageable(), all.getTotalElements());
	}

}
