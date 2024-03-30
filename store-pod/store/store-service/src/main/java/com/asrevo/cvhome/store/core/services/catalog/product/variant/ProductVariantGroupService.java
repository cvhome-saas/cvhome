package com.asrevo.cvhome.store.core.services.catalog.product.variant;

import com.asrevo.cvhome.store.core.entity.catalog.product.variant.ProductVariantGroup;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityService;
import org.springframework.data.domain.Page;

import java.util.Optional;

public interface ProductVariantGroupService extends SalesManagerEntityService<Long, ProductVariantGroup> {


    Optional<ProductVariantGroup> getById(Long id, MerchantStore store);

    Optional<ProductVariantGroup> getByProductVariant(Long productVariantId, MerchantStore store, Language language);

    Page<ProductVariantGroup> getByProductId(MerchantStore store, Long productId, Language language, int page, int count);

    ProductVariantGroup saveOrUpdate(ProductVariantGroup entity) throws ServiceException;


}
