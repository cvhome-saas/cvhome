package com.asrevo.cvhome.store.service.facade.product;

import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.model.catalog.product.product.definition.PersistableProductDefinition;
import com.asrevo.cvhome.store.core.model.catalog.product.product.definition.ReadableProductDefinition;

public interface ProductDefinitionFacade {

    /**
     */
    Long saveProductDefinition(
            MerchantStore store, PersistableProductDefinition product, Language language);

    /**
     */
    void update(
            Long productId,
            PersistableProductDefinition product,
            MerchantStore merchant,
            Language language);

    /**
     */
    ReadableProductDefinition getProduct(MerchantStore store, Long id, Language language);

    /**
     */
    ReadableProductDefinition getProductBySku(
            MerchantStore store, String uniqueCode, Language language);
}
