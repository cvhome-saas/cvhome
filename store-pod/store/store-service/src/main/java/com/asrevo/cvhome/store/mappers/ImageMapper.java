package com.asrevo.cvhome.store.mappers;

import com.asrevo.cvhome.store.commons.dto.ImageDto;
import com.asrevo.cvhome.store.entity.ImageEntity;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

import javax.annotation.processing.Generated;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
@lombok.Generated
@Generated("aas")
public interface ImageMapper {
    ImageDto toDto(ImageEntity entity);

}
