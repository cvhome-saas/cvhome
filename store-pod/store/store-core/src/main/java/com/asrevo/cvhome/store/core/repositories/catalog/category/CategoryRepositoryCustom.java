package com.asrevo.cvhome.store.core.repositories.catalog.category;

import com.asrevo.cvhome.store.core.entity.catalog.category.Category;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import java.util.List;

public interface CategoryRepositoryCustom {

    List<Object[]> countProductsByCategories(MerchantStore store, List<Long> categoryIds);

    List<Category> listByStoreAndParent(MerchantStore store, Category category);

    List<Category> listByProduct(MerchantStore store, Long product);
}
