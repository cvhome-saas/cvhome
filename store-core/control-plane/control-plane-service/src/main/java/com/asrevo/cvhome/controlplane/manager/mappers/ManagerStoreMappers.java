package com.asrevo.cvhome.controlplane.manager.mappers;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.controlplane.manager.commons.dto.ListManagerStoreQuery;
import com.asrevo.cvhome.controlplane.manager.commons.dto.ManagerStoreDto;
import com.asrevo.cvhome.controlplane.manager.entity.ManagerStoreEntity;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

@Mapper(componentModel = "spring")
public interface ManagerStoreMappers {

	ManagerStoreDto toDto(ManagerStoreEntity entity);

	@Mapping(target = "new", ignore = true)
	@Mapping(target = "createdDate", ignore = true)
	@Mapping(target = "version", ignore = true)
	@Mapping(target = "podId", ignore = true)
	@Mapping(target = "orgId", ignore = true)
	@Mapping(target = "provisioningState", ignore = true)
	ManagerStoreEntity toEntity(ListManagerStoreQuery managerStoreDto);

	default Map<Object, Object> toExternalCreateRequest(Map<Object, Object> request, ManagerOrgId orgId,
			ManagerStoreId managerStoreId) {
		HashMap<Object, Object> newRequest = new HashMap<>(request);
		newRequest.put("id", managerStoreId.id().toString());
		newRequest.put("org", orgId.id().toString());
		return newRequest;
	}

	default PageImpl<Object> toPage(List<Object> it, Page<ManagerStoreDto> internalStores) {
		return new PageImpl<>(it, internalStores.getPageable(), internalStores.getTotalElements());
	}

}
