package com.asrevo.cvhome.product.service;

import com.asrevo.cvhome.product.commons.dto.CategoryDto;
import com.asrevo.cvhome.product.commons.dto.CreateCategoryDto;
import com.asrevo.cvhome.product.commons.dto.CreateCategoryResponseDto;
import com.asrevo.cvhome.store.commons.domain.StoreId;
import com.asrevo.cvhome.storepod.commons.domain.CategoryId;

public interface AdminCategoryService {
    CreateCategoryResponseDto createCategory(StoreId storeId, CreateCategoryDto createCategoryDto);

    CreateCategoryResponseDto createCategory(StoreId storeId, CategoryId categoryId, CreateCategoryDto createCategoryDto);


    CategoryDto findCategory(CategoryId categoryId, StoreId storeId);
}
