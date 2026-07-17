package com.asrevo.cvhome.catalog.services.product.attribute;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.asrevo.cvhome.catalog.entity.product.attribute.ProductOption;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityService;

public interface ProductOptionService extends SalesManagerEntityService<Long, ProductOption> {

    void saveOrUpdate(ProductOption entity) throws ServiceException;

    ProductOption getByCode(StoreMerchantId store, String optionCode);

    ProductOption getById(StoreMerchantId store, Long optionId);

    Page<ProductOption> getByMerchant(StoreMerchantId store, LanguageCode language, String name, Pageable pageable);

}
