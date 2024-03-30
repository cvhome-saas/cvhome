package com.asrevo.cvhome.store.core.services.catalog.product.price;

import com.asrevo.cvhome.store.core.entity.catalog.product.price.ProductPrice;
import com.asrevo.cvhome.store.core.entity.catalog.product.price.ProductPriceDescription;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityService;

import java.util.List;

public interface ProductPriceService extends SalesManagerEntityService<Long, ProductPrice> {

    void addDescription(ProductPrice price, ProductPriceDescription description) throws ServiceException;

    ProductPrice saveOrUpdate(ProductPrice price) throws ServiceException;

    List<ProductPrice> findByProductSku(String sku, MerchantStore store);

    ProductPrice findById(Long priceId, String sku, MerchantStore store);

    List<ProductPrice> findByInventoryId(Long productInventoryId, String sku, MerchantStore store);


}
