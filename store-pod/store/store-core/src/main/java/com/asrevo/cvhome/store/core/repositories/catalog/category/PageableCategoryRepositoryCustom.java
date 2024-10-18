package com.asrevo.cvhome.store.core.repositories.catalog.category;

import com.asrevo.cvhome.store.core.entity.catalog.category.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PageableCategoryRepositoryCustom {

    Page<Category> listByStore(Integer storeId, Integer languageId, String name, Pageable pageable);
}
