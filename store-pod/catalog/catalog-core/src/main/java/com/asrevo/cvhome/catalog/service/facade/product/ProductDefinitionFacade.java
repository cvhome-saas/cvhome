package com.asrevo.cvhome.catalog.service.facade.product;

import com.asrevo.cvhome.catalog.model.product.product.definition.PersistableProductDefinition;
import com.asrevo.cvhome.catalog.model.product.product.definition.ReadableProductDefinition;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;

public interface ProductDefinitionFacade {

    /**
     *
     */
    Long saveProductDefinition(StoreMerchantId store, PersistableProductDefinition product, LanguageCode language);

    /**
     *
     */
    void update(Long productId, PersistableProductDefinition product, StoreMerchantId merchant, LanguageCode language);

    /**
     *
     */
    ReadableProductDefinition getProduct(StoreMerchantId store, Long id, LanguageCode language);

}
