package com.asrevo.cvhome.catalog.services.product.variant;

import com.asrevo.cvhome.catalog.entity.product.variant.ProductVariantImage;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityService;
import java.util.List;

public interface ProductVariantImageService extends SalesManagerEntityService<Long, ProductVariantImage> {

	List<ProductVariantImage> listByProductVariantGroup(Long productVariantGroupId, StoreMerchantId store);

}
