package com.asrevo.cvhome.catalog.services.product.price;

import java.util.List;

import com.asrevo.cvhome.catalog.entity.product.price.ProductPrice;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityService;

public interface ProductPriceService extends SalesManagerEntityService<Long, ProductPrice> {

    ProductPrice saveOrUpdate(ProductPrice price);

    List<ProductPrice> findByProductSku(String sku, StoreMerchantId store);

    ProductPrice findById(Long priceId, String sku, StoreMerchantId store);

    List<ProductPrice> findByInventoryId(Long productInventoryId, String sku, StoreMerchantId store);

}
