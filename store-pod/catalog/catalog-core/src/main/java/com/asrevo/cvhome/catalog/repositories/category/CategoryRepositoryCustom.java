package com.asrevo.cvhome.catalog.repositories.category;

import java.util.List;

import com.asrevo.cvhome.catalog.entity.category.Category;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

public interface CategoryRepositoryCustom {

    List<Category> listByProduct(StoreMerchantId store, Long product);

}
