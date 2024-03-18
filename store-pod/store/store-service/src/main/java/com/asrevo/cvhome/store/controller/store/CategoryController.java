package com.asrevo.cvhome.store.controller.store;

import com.asrevo.cvhome.manager.commons.domain.StoreId;
import com.asrevo.cvhome.store.service.CategoryService;
import com.asrevo.cvhome.storepod.commons.dto.CategoriesView;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/v1/public/category")
@AllArgsConstructor
@Slf4j
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping("store-categories-view")
    public List<CategoriesView> getStoreCategoriesView(@RequestParam StoreId storeId) {
        return categoryService.getStoreCategoriesView(storeId);
    }
}

