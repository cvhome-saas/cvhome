package com.asrevo.cvhome.catalog.services.group;

import org.springframework.data.domain.Pageable;

import com.asrevo.cvhome.catalog.errors.ProductGroupNotFoundException;
import com.asrevo.cvhome.catalog.errors.ProductNotFoundException;
import com.asrevo.cvhome.catalog.model.group.PersistableProductGroup;
import com.asrevo.cvhome.catalog.model.group.ReadableProductGroup;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.model.entity.ReadableEntityList;

/**
 * Product groups: the store's merchandising strips, and each product's related items (the {@code RELATED_ITEM}
 * group under that product).
 */
public interface ProductGroupService {

    String RELATED_ITEMS = "RELATED_ITEM";

    ReadableEntityList<ReadableProductGroup> list(StoreMerchantId store, LanguageCode language, Pageable pageable);

    ReadableProductGroup get(StoreMerchantId store, String code, LanguageCode language, boolean allLanguages)
            throws ProductGroupNotFoundException;

    /**
     * The strip as the storefront reads it: the group, or an empty, inactive strip when the store has none by that
     * code. A store that has not set up {@code FEATURED_ITEMS} has nothing to show there, which is not an error —
     * answering 404 made every home page render log a failed call and count it against the service graph.
     */
    ReadableProductGroup storefront(StoreMerchantId store, String code, LanguageCode language);

    boolean exists(StoreMerchantId store, String code);

    /**
     * Creates or, when {@code id} is set, updates a group; the body echoes back with its id.
     */
    PersistableProductGroup save(StoreMerchantId store, PersistableProductGroup group)
            throws ProductGroupNotFoundException, ProductNotFoundException;

    void delete(StoreMerchantId store, String code) throws ProductGroupNotFoundException;

    void addProduct(StoreMerchantId store, String code, Long productId)
            throws ProductGroupNotFoundException, ProductNotFoundException;

    void removeProduct(StoreMerchantId store, String code, Long productId) throws ProductGroupNotFoundException;

    /**
     * The product's related items, or an empty, inactive strip when it has never had any — see
     * {@link #storefront(StoreMerchantId, String, LanguageCode)}.
     */
    ReadableProductGroup related(StoreMerchantId store, Long productId, LanguageCode language);

    /**
     * Relates {@code relatedProductId} to {@code productId}, creating the product's related-items group on first use.
     */
    void addRelated(StoreMerchantId store, Long productId, Long relatedProductId) throws ProductNotFoundException;

    void removeRelated(StoreMerchantId store, Long productId, Long relatedProductId)
            throws ProductGroupNotFoundException;
}
