package com.asrevo.cvhome.landing.service;


import com.asrevo.cvhome.manager.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.storepod.commons.dto.CategoriesView;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.List;

@HttpExchange("api/v1/public/category")
public interface CategoryService {
    @GetExchange("store-categories-view")
    List<CategoriesView> getStoreCategoriesView(ManagerStoreId storeId);
}
