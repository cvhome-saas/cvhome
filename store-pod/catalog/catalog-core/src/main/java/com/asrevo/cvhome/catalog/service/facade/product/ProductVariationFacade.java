package com.asrevo.cvhome.catalog.service.facade.product;

import org.springframework.data.domain.Pageable;

import com.asrevo.cvhome.catalog.model.product.variation.PersistableProductVariation;
import com.asrevo.cvhome.catalog.model.product.variation.ReadableProductVariation;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.model.entity.ReadableEntityList;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;

public interface ProductVariationFacade {

    ReadableProductVariation get(Long variationId, StoreMerchantId store, LanguageCode language);

    boolean exists(String code, StoreMerchantId store);

    Long create(PersistableProductVariation optionSet, StoreMerchantId store, LanguageCode language);

    void update(Long variationId, PersistableProductVariation variation, StoreMerchantId store, LanguageCode language);

    void delete(Long variation, StoreMerchantId store);

    ReadableEntityList<ReadableProductVariation> list(StoreMerchantId store, LanguageCode language, Pageable pageable);

}
