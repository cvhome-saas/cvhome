package com.asrevo.cvhome.store.core.services.catalog.product.type;

import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityService;
import com.asrevo.cvhome.store.core.entity.catalog.product.type.ProductType;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ProductTypeService extends SalesManagerEntityService<Long, ProductType> {

    ProductType getProductType(String productTypeCode);

    Page<ProductType> getByMerchant(MerchantStore store, Language language, int page, int count) throws ServiceException;

    ProductType getByCode(String code, MerchantStore store, Language language) throws ServiceException;

    ProductType getById(Long id, MerchantStore store, Language language) throws ServiceException;

    ProductType getById(Long id, MerchantStore store) throws ServiceException;

    void update(String code, MerchantStore store, ProductType type) throws ServiceException;

    ProductType saveOrUpdate(ProductType productType) throws ServiceException;

    List<ProductType> listProductTypes(List<Long> ids, MerchantStore store, Language language) throws ServiceException;


}
