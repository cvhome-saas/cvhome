package com.asrevo.cvhome.catalog.service.facade.product;

import org.springframework.data.domain.Pageable;

import com.asrevo.cvhome.catalog.errors.InventoryNotConvertibleException;
import com.asrevo.cvhome.catalog.errors.ProductNotConvertibleException;
import com.asrevo.cvhome.catalog.errors.ProductNotFoundException;
import com.asrevo.cvhome.catalog.errors.ProductPriceNotConvertibleException;
import com.asrevo.cvhome.catalog.errors.ProductReferenceUnresolvableException;
import com.asrevo.cvhome.catalog.errors.ProductVariantNotFoundException;
import com.asrevo.cvhome.catalog.errors.ProductVariantParentMissingException;
import com.asrevo.cvhome.catalog.errors.ProductVariantSkuConflictException;
import com.asrevo.cvhome.catalog.errors.ProductVariationOptionsIdenticalException;
import com.asrevo.cvhome.catalog.errors.ProductVariationReferenceUnresolvableException;
import com.asrevo.cvhome.catalog.model.product.product.variant.PersistableProductVariant;
import com.asrevo.cvhome.catalog.model.product.product.variant.ReadableProductVariant;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.model.entity.ReadableEntityList;

public interface ProductVariantFacade {

    ReadableProductVariant get(Long instanceId, Long productId, StoreMerchantId store, LanguageCode language)
            throws ProductVariantNotFoundException, ProductVariantParentMissingException, InventoryNotConvertibleException;

    boolean exists(String sku, StoreMerchantId store, Long productId, LanguageCode language)
            throws ProductNotFoundException, ProductNotConvertibleException,
            ProductPriceNotConvertibleException, ProductVariantParentMissingException,
            InventoryNotConvertibleException;

    Long create(PersistableProductVariant productVariant, Long productId, StoreMerchantId store, LanguageCode language)

            throws ProductVariationOptionsIdenticalException, ProductVariationReferenceUnresolvableException,
            ProductReferenceUnresolvableException,
            ProductVariantSkuConflictException, ProductPriceNotConvertibleException,
            InventoryNotConvertibleException;

    void update(Long instanceId, PersistableProductVariant instance, Long productId, StoreMerchantId store,
                LanguageCode language)
            throws ProductVariantNotFoundException, ProductVariationReferenceUnresolvableException, ProductReferenceUnresolvableException,
            ProductVariantSkuConflictException, ProductPriceNotConvertibleException,
            InventoryNotConvertibleException;

    void delete(Long productVariant, Long productId, StoreMerchantId store)
            throws ProductVariantNotFoundException;

    ReadableEntityList<ReadableProductVariant> list(Long productId, StoreMerchantId store, LanguageCode language,
                                                    Pageable pageable)
            throws ProductNotFoundException, ProductVariantParentMissingException, InventoryNotConvertibleException;

}
