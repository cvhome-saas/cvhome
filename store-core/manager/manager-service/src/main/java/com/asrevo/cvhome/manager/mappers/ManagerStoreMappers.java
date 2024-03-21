package com.asrevo.cvhome.manager.mappers;

import com.asrevo.cvhome.manager.commons.dto.ManagerStoreDto;
import com.asrevo.cvhome.manager.entity.ManagerStoreEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ManagerStoreMappers {
    ManagerStoreDto toDto(ManagerStoreEntity entity);

    List<ManagerStoreDto> toDto(List<ManagerStoreEntity> entities);

    ManagerStoreEntity toEntity(ManagerStoreDto managerStoreDto);
}
