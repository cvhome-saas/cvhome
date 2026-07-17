package com.asrevo.cvhome.catalog.services.product.attribute;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.asrevo.cvhome.catalog.entity.product.attribute.ProductOptionValue;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityService;

public interface ProductOptionValueService extends SalesManagerEntityService<Long, ProductOptionValue> {

    void saveOrUpdate(ProductOptionValue entity) throws ServiceException;

    ProductOptionValue getByCode(StoreMerchantId store, String optionValueCode);

    ProductOptionValue getById(StoreMerchantId store, Long optionValueId);

    Page<ProductOptionValue> getByMerchant(StoreMerchantId store, LanguageCode language, String name,
                                           Pageable pageable);

}
