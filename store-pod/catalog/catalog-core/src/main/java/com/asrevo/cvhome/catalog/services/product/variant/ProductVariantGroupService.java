package com.asrevo.cvhome.catalog.services.product.variant;

import com.asrevo.cvhome.catalog.entity.product.variant.ProductVariantGroup;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityService;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductVariantGroupService extends SalesManagerEntityService<Long, ProductVariantGroup> {

	Optional<ProductVariantGroup> getById(Long id, StoreMerchantId store);

	Page<ProductVariantGroup> getByProductId(StoreMerchantId store, Long productId, LanguageCode language,
			Pageable pageable);

	ProductVariantGroup saveOrUpdate(ProductVariantGroup entity);

}
