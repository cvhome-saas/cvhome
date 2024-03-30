package com.asrevo.cvhome.store.core.services.catalog.product.attribute;

import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityService;
import com.asrevo.cvhome.store.core.entity.catalog.product.attribute.ProductOptionValue;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ProductOptionValueService extends SalesManagerEntityService<Long, ProductOptionValue> {

    void saveOrUpdate(ProductOptionValue entity) throws ServiceException;

    List<ProductOptionValue> getByName(MerchantStore store, String name,
                                       Language language) throws ServiceException;


    List<ProductOptionValue> listByStore(MerchantStore store, Language language)
            throws ServiceException;

    List<ProductOptionValue> listByStoreNoReadOnly(MerchantStore store,
                                                   Language language) throws ServiceException;

    ProductOptionValue getByCode(MerchantStore store, String optionValueCode);

    ProductOptionValue getById(MerchantStore store, Long optionValueId);

    Page<ProductOptionValue> getByMerchant(MerchantStore store, Language language, String name, int page, int count);


}
