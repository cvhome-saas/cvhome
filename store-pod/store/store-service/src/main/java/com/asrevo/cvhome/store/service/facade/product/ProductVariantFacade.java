package com.asrevo.cvhome.store.service.facade.product;

import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.model.catalog.product.product.variant.PersistableProductVariant;
import com.asrevo.cvhome.store.core.model.catalog.product.product.variant.ReadableProductVariant;
import com.asrevo.cvhome.store.core.model.entity.ReadableEntityList;

public interface ProductVariantFacade {

    ReadableProductVariant get(
            Long instanceId, Long productId, MerchantStore store, Language language);

    boolean exists(String sku, MerchantStore store, Long productId, Language language);

    Long create(
            PersistableProductVariant productVariant,
            Long productId,
            MerchantStore store,
            Language language);

    void update(
            Long instanceId,
            PersistableProductVariant instance,
            Long productId,
            MerchantStore store,
            Language language);

    void delete(Long productVariant, Long productId, MerchantStore store);

    ReadableEntityList<ReadableProductVariant> list(
            Long productId, MerchantStore store, Language language, int page, int count);
}
