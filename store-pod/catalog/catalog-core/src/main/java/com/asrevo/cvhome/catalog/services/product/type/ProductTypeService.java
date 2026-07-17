package com.asrevo.cvhome.catalog.services.product.type;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.asrevo.cvhome.catalog.entity.product.type.ProductType;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityService;

public interface ProductTypeService extends SalesManagerEntityService<Long, ProductType> {

    Page<ProductType> getByMerchant(StoreMerchantId store, LanguageCode language, Pageable pageable);

    ProductType getByCode(String code, StoreMerchantId store, LanguageCode language);

    ProductType getById(Long id, StoreMerchantId store);

    ProductType saveOrUpdate(ProductType productType) throws ServiceException;

    List<ProductType> listProductTypes(List<Long> ids, StoreMerchantId store, LanguageCode language);

}
