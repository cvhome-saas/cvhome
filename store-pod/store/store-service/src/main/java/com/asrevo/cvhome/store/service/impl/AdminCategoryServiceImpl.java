package com.asrevo.cvhome.store.service.impl;

import com.asrevo.cvhome.storepod.commons.domain.CategoryId;
import com.asrevo.cvhome.storepod.commons.dto.CategoriesView;
import com.asrevo.cvhome.store.commons.dto.CategoryDto;
import com.asrevo.cvhome.store.commons.dto.CreateCategoryDto;
import com.asrevo.cvhome.store.commons.dto.CreateCategoryResponseDto;
import com.asrevo.cvhome.store.entity.CategoryEntity;
import com.asrevo.cvhome.store.mappers.CategoryMapper;
import com.asrevo.cvhome.store.repository.CategoryRepository;
import com.asrevo.cvhome.store.service.AdminCategoryService;
import com.asrevo.cvhome.store.utils.ErrorCodes;
import com.asrevo.cvhome.commons.utils.OperationExecution;
import com.asrevo.cvhome.manager.commons.domain.StoreId;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class AdminCategoryServiceImpl implements AdminCategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Transactional
    @Override
    public CreateCategoryResponseDto createCategory(StoreId storeId, CreateCategoryDto createCategoryDto) {
        CategoryEntity saved = categoryRepository.save(CategoryEntity.createCategory(storeId, createCategoryDto));
        return categoryMapper.toCreateCategoryResponse(saved);
    }

    @Transactional
    @Override
    public CreateCategoryResponseDto createCategory(StoreId storeId, CategoryId parent, CreateCategoryDto createCategoryDto) {
        CategoryEntity parentCategory = categoryRepository.findByIdAndStoreId(parent, storeId)
                .orElseThrow(() -> new OperationExecution(ErrorCodes.parent_category_not_exist));
        CategoryEntity savedCategory = categoryRepository.save(CategoryEntity.createCategory(storeId, parentCategory, createCategoryDto));
        return categoryMapper.toCreateCategoryResponse(savedCategory);
    }

    /*
     *
     *    123   null
     *    456   null
     *    111   123
     *    222   123
     *    333   456
     *    444   456
     *
     *
     *
     *
     *       123             456
     *    111    222     333       444
     *
     * */


    @Override
    public CategoryDto findCategory(CategoryId categoryId, StoreId storeId) {
        CategoryEntity categoryEntity = categoryRepository.findByIdAndStoreId(categoryId, storeId)
                .orElseThrow(() -> new OperationExecution(ErrorCodes.category_not_exist));
        return categoryMapper.toDto(categoryEntity);
    }
}
