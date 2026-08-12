package com.asrevo.cvhome.tenancy.manager.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import com.asrevo.cvhome.tenancy.commons.dto.ListManagerStoreQuery;
import com.asrevo.cvhome.tenancy.commons.dto.ManagerStoreDto;
import com.asrevo.cvhome.tenancy.manager.entity.ManagerStoreEntity;

@Mapper(componentModel = "spring")
public interface ManagerStoreMappers {

    @Mapping(target = "billingStatus", ignore = true)
    ManagerStoreDto toDto(ManagerStoreEntity entity);

    @Mapping(target = "new", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "podId", ignore = true)
    @Mapping(target = "orgId", ignore = true)
    @Mapping(target = "provisioningState", ignore = true)
    // A list filter has no lifecycle status: the query excludes DELETED itself and matches the rest.
    @Mapping(target = "status", ignore = true)
    ManagerStoreEntity toEntity(ListManagerStoreQuery managerStoreDto);

    default PageImpl<Object> toPage(List<Object> it, Page<ManagerStoreDto> internalStores) {
        return new PageImpl<>(it, internalStores.getPageable(), internalStores.getTotalElements());
    }

}
