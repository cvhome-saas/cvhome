package com.asrevo.cvhome.product.mappers;

import com.asrevo.cvhome.product.commons.dto.CreateProductResponseDto;
import com.asrevo.cvhome.product.commons.dto.ProductDto;
import com.asrevo.cvhome.product.commons.dto.UpdateProductDto;
import com.asrevo.cvhome.product.commons.dto.UpdateProductResponseDto;
import com.asrevo.cvhome.product.entity.ProductEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProductMapper {
    ProductDto toDto(ProductEntity entity);

    CreateProductResponseDto toCreateProductResponseDto(ProductEntity entity);

    @Mapping(target = "new", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "storeId", ignore = true)
    @Mapping(target = "productVariants", ignore = true)
    @Mapping(target = "isNew", ignore = true)
    void map(UpdateProductDto updateProductDto, @MappingTarget ProductEntity entity);

    UpdateProductResponseDto toUpdateProductResponseDto(ProductEntity entity);
}
