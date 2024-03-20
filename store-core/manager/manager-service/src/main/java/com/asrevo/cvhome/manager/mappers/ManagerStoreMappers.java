package com.asrevo.cvhome.manager.mappers;

import com.asrevo.cvhome.manager.commons.dto.ManagerStoreDto;
import com.asrevo.cvhome.manager.entity.ManagerStoreEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ManagerStoreMappers {
    ManagerStoreDto toStoreDto(ManagerStoreEntity entity);

    List<ManagerStoreDto> toStoreDto(List<ManagerStoreEntity> entities);
}
