package com.asrevo.cvhome.store.core.services.catalog.product.variant;

import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityService;
import com.asrevo.cvhome.store.core.entity.catalog.product.variant.ProductVariantImage;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;

import java.util.List;

public interface ProductVariantImageService extends SalesManagerEntityService<Long, ProductVariantImage> {


    List<ProductVariantImage> list(Long productVariantId, MerchantStore store);

    List<ProductVariantImage> listByProduct(Long productId, MerchantStore store);

    List<ProductVariantImage> listByProductVariantGroup(Long productVariantGroupId, MerchantStore store);

}
