package com.asrevo.cvhome.store.core.services.catalog.product.attribute;

import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityService;
import com.asrevo.cvhome.store.core.entity.catalog.product.attribute.ProductOptionSet;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;

import java.util.List;

public interface ProductOptionSetService extends SalesManagerEntityService<Long, ProductOptionSet> {

    List<ProductOptionSet> listByStore(MerchantStore store, Language language)
            throws ServiceException;


    ProductOptionSet getById(MerchantStore store, Long optionId, Language lang);

    ProductOptionSet getCode(MerchantStore store, String code);

    List<ProductOptionSet> getByProductType(Long productTypeId, MerchantStore store, Language lang);


}
