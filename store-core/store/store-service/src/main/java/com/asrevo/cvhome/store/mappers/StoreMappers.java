package com.asrevo.cvhome.store.mappers;

import com.asrevo.cvhome.store.commons.dto.StoreDto;
import com.asrevo.cvhome.store.entity.StoreEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface StoreMappers {
    @Mapping(source = "owner.id", target = "owner")
    StoreDto toStoreDto(StoreEntity entity);

    List<StoreDto> toStoreDto(List<StoreEntity> entities);
}
