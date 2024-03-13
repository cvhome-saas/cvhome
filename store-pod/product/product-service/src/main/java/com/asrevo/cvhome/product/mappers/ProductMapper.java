package com.asrevo.cvhome.product.mappers;

import com.asrevo.cvhome.product.commons.domain.ProductId;
import com.asrevo.cvhome.product.commons.dto.*;
import com.asrevo.cvhome.product.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProductMapper {
    @Mapping(source = "productImages", target = "images")
    ProductDto toDto(ProductEntity entity);

    default List<String> toImages(List<ProductImageEntity> images) {
        return images.stream().map(ProductImageEntity::getLink).toList();
    }

    CreateProductResponseDto toCreateProductResponseDto(ProductEntity entity);

    @Mapping(target = "new", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "storeId", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "isNew", ignore = true)
    @Mapping(target = "productImages", ignore = true)
    void map(UpdateProductDto updateProductDto, @MappingTarget ProductEntity entity);

    UpdateProductResponseDto toUpdateProductResponseDto(ProductEntity entity);

    AddProductVariantResponseDto toAddProductVariantResponseDto(ProductVariantEntity entity);

    default Map<String, String> toMap(List<ProductVariantFeatureEntity> features) {
        HashMap<String, String> map = new HashMap<>();
        features.forEach(it -> map.put(it.getKey(), it.getValue()));
        return map;
    }

    ProductVariantDto toDto(ProductVariantEntity entity);

    default ProductId toProductId(ProductVariantRefEntity entity) {
        return entity.getProduct().getId();
    }
}
