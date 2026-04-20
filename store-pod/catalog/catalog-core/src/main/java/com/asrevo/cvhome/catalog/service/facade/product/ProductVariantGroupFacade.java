package com.asrevo.cvhome.catalog.service.facade.product;

import org.springframework.data.domain.Pageable;

import com.asrevo.cvhome.catalog.model.product.product.variant.PersistableProductVariantGroup;
import com.asrevo.cvhome.catalog.model.product.product.variant.ReadableProductVariantGroup;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.model.entity.ReadableEntityList;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;

public interface ProductVariantGroupFacade {

    ReadableProductVariantGroup get(Long instanceGroupId, StoreMerchantId store, LanguageCode language);

    Long create(PersistableProductVariantGroup productVariantGroup, StoreMerchantId store, LanguageCode language);

    void update(Long productVariantGroup, PersistableProductVariantGroup instance, StoreMerchantId store,
                LanguageCode language);

    void delete(Long productVariant, Long productId, StoreMerchantId store);

    ReadableEntityList<ReadableProductVariantGroup> list(Long productId, StoreMerchantId store, LanguageCode language,
                                                         Pageable pageable);

}
