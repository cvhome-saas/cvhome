package com.asrevo.cvhome.store.service;

import com.asrevo.cvhome.store.commons.dto.CategoryDto;
import com.asrevo.cvhome.store.commons.dto.CreateCategoryDto;
import com.asrevo.cvhome.store.commons.dto.CreateCategoryResponseDto;
import com.asrevo.cvhome.manager.commons.domain.StoreId;
import com.asrevo.cvhome.storepod.commons.domain.CategoryId;

public interface AdminCategoryService {
    CreateCategoryResponseDto createCategory(StoreId storeId, CreateCategoryDto createCategoryDto);

    CreateCategoryResponseDto createCategory(StoreId storeId, CategoryId categoryId, CreateCategoryDto createCategoryDto);


    CategoryDto findCategory(CategoryId categoryId, StoreId storeId);
}
