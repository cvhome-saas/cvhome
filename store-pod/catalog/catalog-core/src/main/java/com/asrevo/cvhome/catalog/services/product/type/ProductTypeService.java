package com.asrevo.cvhome.catalog.services.product.type;

import com.asrevo.cvhome.catalog.entity.product.type.ProductType;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityService;
import java.util.List;
import org.springframework.data.domain.Page;

public interface ProductTypeService extends SalesManagerEntityService<Long, ProductType> {

    Page<ProductType> getByMerchant(
            StoreMerchantId store, LanguageCode language, int page, int count);

    ProductType getByCode(String code, StoreMerchantId store, LanguageCode language);

    ProductType getById(Long id, StoreMerchantId store, LanguageCode language);

    ProductType getById(Long id, StoreMerchantId store);

    ProductType saveOrUpdate(ProductType productType) throws ServiceException;

    List<ProductType> listProductTypes(
            List<Long> ids, StoreMerchantId store, LanguageCode language);
}
