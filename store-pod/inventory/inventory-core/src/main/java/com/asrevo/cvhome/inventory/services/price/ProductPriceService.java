package com.asrevo.cvhome.inventory.services.price;

import java.util.List;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.inventory.entity.ProductPrice;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityService;

public interface ProductPriceService extends SalesManagerEntityService<Long, ProductPrice> {

    ProductPrice saveOrUpdate(ProductPrice price);

    List<ProductPrice> findByProductSku(String sku, StoreMerchantId store);

    ProductPrice findById(Long priceId, String sku, StoreMerchantId store);

    List<ProductPrice> findByInventoryId(Long productInventoryId, String sku, StoreMerchantId store);

}
