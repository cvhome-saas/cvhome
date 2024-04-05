package com.asrevo.cvhome.store.service.facade.product;

import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.model.catalog.product.product.definition.PersistableProductDefinition;
import com.asrevo.cvhome.store.core.model.catalog.product.product.definition.ReadableProductDefinition;

public interface ProductDefinitionFacade {

    /**
     * @param store
     * @param product
     * @param language
     * @return
     */
    Long saveProductDefinition(MerchantStore store, PersistableProductDefinition product, Language language);

    /**
     * @param productId
     * @param product
     * @param merchant
     * @param language
     */
    void update(Long productId, PersistableProductDefinition product, MerchantStore merchant, Language language);

    /**
     * @param store
     * @param id
     * @param language
     * @return
     */
    ReadableProductDefinition getProduct(MerchantStore store, Long id, Language language);

    /**
     * @param store
     * @param uniqueCode
     * @param language
     * @return
     */
    ReadableProductDefinition getProductBySku(MerchantStore store, String uniqueCode, Language language);

}
