package com.asrevo.cvhome.catalog.services.variant;

import java.util.List;

import com.asrevo.cvhome.catalog.errors.DuplicateVariantCombinationException;
import com.asrevo.cvhome.catalog.errors.DuplicateVariantSkuException;
import com.asrevo.cvhome.catalog.errors.ProductNotFoundException;
import com.asrevo.cvhome.catalog.errors.ProductOptionNotFoundException;
import com.asrevo.cvhome.catalog.errors.VariantLimitExceededException;
import com.asrevo.cvhome.catalog.errors.VariantOptionsInvalidException;
import com.asrevo.cvhome.catalog.model.product.PersistableVariantSet;
import com.asrevo.cvhome.catalog.model.product.ReadableProductVariantDefinition;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

/**
 * A product's variant set — axes and combinations written together as one atomic replace, so they can never
 * disagree, and every product keeps its invariant of owning at least one variant.
 */
public interface ProductVariantService {

    List<ReadableProductVariantDefinition> list(StoreMerchantId store, Long productId, LanguageCode language)
            throws ProductNotFoundException;

    void replaceAll(StoreMerchantId store, Long productId, PersistableVariantSet set)
            throws ProductNotFoundException, ProductOptionNotFoundException, VariantOptionsInvalidException,
            DuplicateVariantSkuException, DuplicateVariantCombinationException, VariantLimitExceededException;
}
