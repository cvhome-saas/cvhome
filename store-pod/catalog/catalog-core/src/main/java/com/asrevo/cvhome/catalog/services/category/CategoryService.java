package com.asrevo.cvhome.catalog.services.category;

import com.asrevo.cvhome.catalog.entity.category.Category;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityService;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CategoryService extends SalesManagerEntityService<Long, Category> {

	List<Category> getListByLineage(StoreMerchantId store, String lineage) throws ServiceException;

	void addChild(Category parent, Category child) throws ServiceException;

	void saveOrUpdate(Category category) throws ServiceException;

	Category getById(Long id, StoreMerchantId merchantId);

	Page<Category> getListByDepth(StoreMerchantId store, LanguageCode language, String name, int depth,
			Pageable pageable);

	Category getByCode(StoreMerchantId storeCode, String code) throws ServiceException;

	Category getBySeUrl(StoreMerchantId store, String seUrl, LanguageCode language);

	Category getOneByLanguage(long categoryId, LanguageCode language);

	List<Category> getByProductId(Long productId, StoreMerchantId store);

}
