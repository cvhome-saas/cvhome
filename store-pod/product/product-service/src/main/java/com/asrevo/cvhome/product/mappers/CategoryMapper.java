package com.asrevo.cvhome.product.mappers;

import com.asrevo.cvhome.product.commons.domain.CategoryId;
import com.asrevo.cvhome.product.commons.dto.CategoryDto;
import com.asrevo.cvhome.product.commons.dto.CreateCategoryResponseDto;
import com.asrevo.cvhome.product.entity.CategoryEntity;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.data.jdbc.core.mapping.AggregateReference;

import java.util.Optional;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CategoryMapper {
    CategoryDto toDto(CategoryEntity entity);
    CreateCategoryResponseDto toCreateCategoryResponse(CategoryEntity entity);


    default CategoryId toCategoryId(AggregateReference<CategoryEntity, CategoryId> parent) {
        return Optional.ofNullable(parent).map(AggregateReference::getId).orElse(null);
    }

}
