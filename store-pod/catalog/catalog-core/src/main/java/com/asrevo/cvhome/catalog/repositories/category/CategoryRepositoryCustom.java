package com.asrevo.cvhome.catalog.repositories.category;

import com.asrevo.cvhome.catalog.entity.category.Category;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import java.util.List;

public interface CategoryRepositoryCustom {

	List<Category> listByProduct(StoreMerchantId store, Long product);

}
