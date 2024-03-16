package com.asrevo.cvhome.product.mappers;

import com.asrevo.cvhome.product.commons.domain.CategoryId;
import com.asrevo.cvhome.product.commons.domain.ProductId;
import com.asrevo.cvhome.product.commons.dto.*;
import com.asrevo.cvhome.product.entity.CategoryEntity;
import com.asrevo.cvhome.product.entity.ProductEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.data.jdbc.core.mapping.AggregateReference;

import javax.annotation.processing.Generated;
import java.util.Optional;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
@lombok.Generated
@Generated("aas")
public interface ProductMapper {
    ProductDto toDto(ProductEntity entity);

    CreateProductResponseDto toCreateProductResponseDto(ProductEntity entity);

    @Mapping(target = "new", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "storeId", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "published", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "isNew", ignore = true)
    void map(UpdateProductDto updateProductDto, @MappingTarget ProductEntity entity);

    UpdateProductResponseDto toUpdateProductResponseDto(ProductEntity entity);


    default ProductId toProductId(AggregateReference<ProductEntity, ProductId> product) {
        return Optional.ofNullable(product).map(AggregateReference::getId).orElse(null);
    }

    default CategoryId toCategoryId(AggregateReference<CategoryEntity, CategoryId> category) {
        return Optional.ofNullable(category).map(AggregateReference::getId).orElse(null);
    }

    default AggregateReference<CategoryEntity, CategoryId> toAggregateReference(CategoryId categoryId) {
        return Optional.ofNullable(categoryId).map(it-> {
            AggregateReference<CategoryEntity, CategoryId> e = AggregateReference.to(categoryId);
            return e;
        }).orElse(null);
    }

    ProductEntity toEntity(FindAllProductDto dto);
}
