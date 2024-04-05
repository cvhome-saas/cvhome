package com.asrevo.cvhome.store.service.facade.product;

import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.model.catalog.product.variation.PersistableProductVariation;
import com.asrevo.cvhome.store.core.model.catalog.product.variation.ReadableProductVariation;
import com.asrevo.cvhome.store.core.model.entity.ReadableEntityList;

public interface ProductVariationFacade {


    ReadableProductVariation get(Long variationId, MerchantStore store, Language language);

    boolean exists(String code, MerchantStore store);

    Long create(PersistableProductVariation optionSet, MerchantStore store, Language language);

    void update(Long variationId, PersistableProductVariation variation, MerchantStore store, Language language);

    void delete(Long variation, MerchantStore store);

    ReadableEntityList<ReadableProductVariation> list(MerchantStore store, Language language, int page, int count);


}
