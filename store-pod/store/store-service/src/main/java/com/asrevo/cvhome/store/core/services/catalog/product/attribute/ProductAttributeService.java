package com.asrevo.cvhome.store.core.services.catalog.product.attribute;

import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityService;
import com.asrevo.cvhome.store.core.entity.catalog.product.Product;
import com.asrevo.cvhome.store.core.entity.catalog.product.attribute.ProductAttribute;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ProductAttributeService extends
        SalesManagerEntityService<Long, ProductAttribute> {

    ProductAttribute saveOrUpdate(ProductAttribute productAttribute)
            throws ServiceException;

    List<ProductAttribute> getByOptionId(MerchantStore store,
                                         Long id) throws ServiceException;

    List<ProductAttribute> getByOptionValueId(MerchantStore store,
                                              Long id) throws ServiceException;

    Page<ProductAttribute> getByProductId(MerchantStore store, Product product, Language language, int page, int count)
            throws ServiceException;

    Page<ProductAttribute> getByProductId(MerchantStore store, Product product, int page, int count)
            throws ServiceException;

    List<ProductAttribute> getByAttributeIds(MerchantStore store, Product product, List<Long> ids)
            throws ServiceException;

    List<ProductAttribute> getProductAttributesByCategoryLineage(MerchantStore store, String lineage, Language language) throws Exception;
}
