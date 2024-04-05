package com.asrevo.cvhome.manager.mappers;

import com.asrevo.cvhome.manager.commons.dto.ListManagerStoreQuery;
import com.asrevo.cvhome.manager.commons.dto.ManagerStoreDto;
import com.asrevo.cvhome.manager.entity.ManagerStoreEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ManagerStoreMappers {
    ManagerStoreDto toDto(ManagerStoreEntity entity);

    @Mapping(target = "new", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "country", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "syncedInRouter", ignore = true)
    @Mapping(target = "syncedInStore", ignore = true)
    @Mapping(target = "isNew", ignore = true)
    ManagerStoreEntity toEntity(ListManagerStoreQuery managerStoreDto);
}
