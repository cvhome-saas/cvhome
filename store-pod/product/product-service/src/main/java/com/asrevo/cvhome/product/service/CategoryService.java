package com.asrevo.cvhome.product.service;

import com.asrevo.cvhome.product.commons.domain.CategoryId;
import com.asrevo.cvhome.product.commons.dto.CategoriesView;
import com.asrevo.cvhome.product.commons.dto.CategoryDto;
import com.asrevo.cvhome.product.commons.dto.CreateCategoryDto;
import com.asrevo.cvhome.product.commons.dto.CreateCategoryResponseDto;
import com.asrevo.cvhome.store.commons.domain.StoreId;

import java.util.List;
import java.util.Optional;

public interface CategoryService {
    CreateCategoryResponseDto createCategory(StoreId storeId, CreateCategoryDto createCategoryDto);

    CreateCategoryResponseDto createCategory(StoreId storeId, CategoryId categoryId, CreateCategoryDto createCategoryDto);

    List<CategoriesView> getStoreCategoriesView(StoreId storeId);

    CategoryDto findCategory(CategoryId categoryId, StoreId storeId);
}
