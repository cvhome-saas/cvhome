package com.asrevo.cvhome.store.service;

import com.asrevo.cvhome.storepod.commons.domain.StoreId;
import com.asrevo.cvhome.storepod.commons.dto.CategoriesView;

import java.util.List;

public interface CategoryService {
    List<CategoriesView> getStoreCategoriesView(StoreId storeId);
}
