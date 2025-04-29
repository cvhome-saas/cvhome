package com.asrevo.cvhome.catalog.services.product.attribute;

import com.asrevo.cvhome.catalog.entity.product.attribute.ProductOptionSet;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityService;
import java.util.List;

public interface ProductOptionSetService extends SalesManagerEntityService<Long, ProductOptionSet> {

    List<ProductOptionSet> listByStore(StoreMerchantId store, LanguageCode language);

    ProductOptionSet getById(StoreMerchantId store, Long optionId, LanguageCode lang);

    ProductOptionSet getCode(StoreMerchantId store, String code);

    List<ProductOptionSet> getByProductType(
            Long productTypeId, StoreMerchantId store, LanguageCode lang);
}
