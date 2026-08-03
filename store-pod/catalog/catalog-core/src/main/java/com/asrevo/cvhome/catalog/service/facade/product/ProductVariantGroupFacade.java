package com.asrevo.cvhome.catalog.service.facade.product;

import org.springframework.data.domain.Pageable;

import com.asrevo.cvhome.catalog.errors.InventoryNotConvertibleException;
import com.asrevo.cvhome.catalog.errors.ProductVariantGroupNotFoundException;
import com.asrevo.cvhome.catalog.errors.ProductVariantNotFoundException;
import com.asrevo.cvhome.catalog.errors.ProductVariantParentMissingException;
import com.asrevo.cvhome.catalog.model.product.product.variant.PersistableProductVariantGroup;
import com.asrevo.cvhome.catalog.model.product.product.variant.ReadableProductVariantGroup;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.entity.ReadableEntityList;

public interface ProductVariantGroupFacade {

    ReadableProductVariantGroup get(Long instanceGroupId, StoreMerchantId store, LanguageCode language)
            throws ProductVariantGroupNotFoundException, ProductVariantParentMissingException, InventoryNotConvertibleException;

    Long create(PersistableProductVariantGroup productVariantGroup, StoreMerchantId store, LanguageCode language);

    void update(Long productVariantGroup, PersistableProductVariantGroup instance, StoreMerchantId store,
                LanguageCode language)
            throws ProductVariantGroupNotFoundException;

    void delete(Long productVariant, Long productId, StoreMerchantId store)
            throws ProductVariantGroupNotFoundException, ProductVariantNotFoundException, ServiceException;

    ReadableEntityList<ReadableProductVariantGroup> list(Long productId, StoreMerchantId store, LanguageCode language,
                                                         Pageable pageable)
            throws ProductVariantParentMissingException, InventoryNotConvertibleException;

}
