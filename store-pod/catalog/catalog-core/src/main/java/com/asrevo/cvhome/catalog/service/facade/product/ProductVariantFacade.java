package com.asrevo.cvhome.catalog.service.facade.product;

import org.springframework.data.domain.Pageable;

import com.asrevo.cvhome.catalog.model.product.product.variant.PersistableProductVariant;
import com.asrevo.cvhome.catalog.model.product.product.variant.ReadableProductVariant;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.model.entity.ReadableEntityList;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;

public interface ProductVariantFacade {

    ReadableProductVariant get(Long instanceId, Long productId, StoreMerchantId store, LanguageCode language);

    boolean exists(String sku, StoreMerchantId store, Long productId, LanguageCode language);

    Long create(PersistableProductVariant productVariant, Long productId, StoreMerchantId store, LanguageCode language);

    void update(Long instanceId, PersistableProductVariant instance, Long productId, StoreMerchantId store,
                LanguageCode language);

    void delete(Long productVariant, Long productId, StoreMerchantId store);

    ReadableEntityList<ReadableProductVariant> list(Long productId, StoreMerchantId store, LanguageCode language,
                                                    Pageable pageable);

}
