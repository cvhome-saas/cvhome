package com.asrevo.cvhome.manager.mappers;

import com.asrevo.cvhome.manager.commons.dto.StoreDto;
import com.asrevo.cvhome.manager.entity.StoreEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface StoreMappers {
    @Mapping(source = "owner.id", target = "owner")
    StoreDto toStoreDto(StoreEntity entity);

    List<StoreDto> toStoreDto(List<StoreEntity> entities);
}
