package com.asrevo.cvhome.product.service;

import com.asrevo.cvhome.store.commons.domain.StoreId;
import com.asrevo.cvhome.storepod.commons.dto.CategoriesView;

import java.util.List;

public interface CategoryService {
    List<CategoriesView> getStoreCategoriesView(StoreId storeId);
}
