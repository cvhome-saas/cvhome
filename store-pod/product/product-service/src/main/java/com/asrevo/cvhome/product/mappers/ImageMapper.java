package com.asrevo.cvhome.product.mappers;

import com.asrevo.cvhome.product.commons.dto.ImageDto;
import com.asrevo.cvhome.product.entity.ImageEntity;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

import javax.annotation.processing.Generated;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
@lombok.Generated
@Generated("aas")
public interface ImageMapper {
    ImageDto toDto(ImageEntity entity);

}
