package com.asrevo.cvhome.catalog.services.category;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.asrevo.cvhome.catalog.entity.category.Category;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityService;

public interface CategoryService extends SalesManagerEntityService<Long, Category> {

    List<Category> getListByLineage(StoreMerchantId store, String lineage) throws ServiceException;

    void addChild(Category parent, Category child) throws ServiceException;

    void saveOrUpdate(Category category) throws ServiceException;

    Category getById(Long id, StoreMerchantId merchantId);

    Page<Category> getListByDepth(StoreMerchantId store, LanguageCode language, String name, int depth,
                                  Pageable pageable);

    Category getByCode(StoreMerchantId storeCode, String code) throws ServiceException;

    Category getBySeUrl(StoreMerchantId store, String seUrl, LanguageCode language);

    List<Category> getByProductId(Long productId, StoreMerchantId store);

}
