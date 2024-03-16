package com.asrevo.cvhome.product.service;

import com.asrevo.cvhome.product.commons.dto.CategoriesView;
import com.asrevo.cvhome.store.commons.domain.StoreId;

import java.util.List;

public interface CategoryService {
    List<CategoriesView> getStoreCategoriesView(StoreId storeId);
}
