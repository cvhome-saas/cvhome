package com.asrevo.cvhome.product.controller.admin;

import com.asrevo.cvhome.storepod.commons.domain.CategoryId;
import com.asrevo.cvhome.product.commons.dto.CreateCategoryDto;
import com.asrevo.cvhome.product.commons.dto.CreateCategoryResponseDto;
import com.asrevo.cvhome.product.service.AdminCategoryService;
import com.asrevo.cvhome.store.commons.domain.StoreId;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("api/v1/admin/category")
@AllArgsConstructor
@Slf4j
public class AdminCategoryController {
    private final AdminCategoryService categoryService;

    @PostMapping("create")
    public CreateCategoryResponseDto createCategory(@RequestParam StoreId storeId, @RequestParam(required = false) CategoryId categoryId, @RequestBody CreateCategoryDto createCategoryDto) {
        return Optional.ofNullable(categoryId)
                .map(it -> categoryService.createCategory(storeId, categoryId, createCategoryDto))
                .orElseGet(() -> categoryService.createCategory(storeId, createCategoryDto));
    }


}

