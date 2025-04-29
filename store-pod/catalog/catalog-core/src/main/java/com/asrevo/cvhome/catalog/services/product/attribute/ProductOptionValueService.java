package com.asrevo.cvhome.catalog.services.product.attribute;

import com.asrevo.cvhome.catalog.entity.product.attribute.ProductOptionValue;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityService;
import org.springframework.data.domain.Page;

public interface ProductOptionValueService
        extends SalesManagerEntityService<Long, ProductOptionValue> {

    void saveOrUpdate(ProductOptionValue entity) throws ServiceException;

    ProductOptionValue getByCode(StoreMerchantId store, String optionValueCode);

    ProductOptionValue getById(StoreMerchantId store, Long optionValueId);

    Page<ProductOptionValue> getByMerchant(
            StoreMerchantId store, LanguageCode language, String name, int page, int count);
}
