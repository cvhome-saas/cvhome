package com.asrevo.cvhome.catalog.service.facade.product;

import org.springframework.data.domain.Pageable;

import com.asrevo.cvhome.catalog.errors.DuplicateProductVariationException;
import com.asrevo.cvhome.catalog.errors.ProductOptionReferenceUnresolvableException;
import com.asrevo.cvhome.catalog.errors.ProductOptionValueReferenceUnresolvableException;
import com.asrevo.cvhome.catalog.errors.ProductVariationNotFoundException;
import com.asrevo.cvhome.catalog.model.product.variation.PersistableProductVariation;
import com.asrevo.cvhome.catalog.model.product.variation.ReadableProductVariation;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.entity.ReadableEntityList;

public interface ProductVariationFacade {

    ReadableProductVariation get(Long variationId, StoreMerchantId store, LanguageCode language)
            throws ProductVariationNotFoundException;

    boolean exists(String code, StoreMerchantId store);

    Long create(PersistableProductVariation optionSet, StoreMerchantId store, LanguageCode language)

            throws DuplicateProductVariationException, ProductOptionReferenceUnresolvableException,
            ProductOptionValueReferenceUnresolvableException, ServiceException;

    void update(Long variationId, PersistableProductVariation variation, StoreMerchantId store, LanguageCode language)

            throws ProductVariationNotFoundException, ProductOptionReferenceUnresolvableException,
            ProductOptionValueReferenceUnresolvableException;

    void delete(Long variation, StoreMerchantId store)
            throws ProductVariationNotFoundException, ServiceException;

    ReadableEntityList<ReadableProductVariation> list(StoreMerchantId store, LanguageCode language, Pageable pageable);

}
