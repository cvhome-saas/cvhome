package com.asrevo.cvhome.manager.mappers;

import com.asrevo.cvhome.manager.commons.dto.ListManagerStoreQuery;
import com.asrevo.cvhome.manager.commons.dto.ManagerStoreDto;
import com.asrevo.cvhome.manager.entity.ManagerStoreEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ManagerStoreMappers {
    ManagerStoreDto toDto(ManagerStoreEntity entity);

    ManagerStoreEntity toEntity(ListManagerStoreQuery managerStoreDto);
}
