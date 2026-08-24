package com.asrevo.cvhome.catalog.services.category;

import org.springframework.data.domain.Pageable;

import com.asrevo.cvhome.catalog.errors.CategoryFriendlyUrlNotFoundException;
import com.asrevo.cvhome.catalog.errors.CategoryNotFoundException;
import com.asrevo.cvhome.catalog.errors.CategoryReferenceUnresolvableException;
import com.asrevo.cvhome.catalog.model.category.PersistableCategory;
import com.asrevo.cvhome.catalog.model.category.ReadableCategory;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.model.entity.ReadableEntityList;

/**
 * The store's category tree. Private reads answer every language; the storefront reads answer one.
 */
public interface CategoryService {

    /**
     * A page of categories as a flat list.
     */
    ReadableEntityList<ReadableCategory> list(StoreMerchantId store, String name, LanguageCode language,
                                              boolean allLanguages, Pageable pageable);

    /**
     * A page of categories as a tree: the roots, each carrying its descendants under {@code children}.
     */
    ReadableEntityList<ReadableCategory> hierarchy(StoreMerchantId store, String name, LanguageCode language,
                                                   boolean allLanguages, Pageable pageable);

    /**
     * One category with every language and its whole subtree under {@code children}.
     */
    ReadableCategory get(StoreMerchantId store, Long id, LanguageCode language) throws CategoryNotFoundException;

    ReadableCategory getByFriendlyUrl(StoreMerchantId store, String friendlyUrl, LanguageCode language)
            throws CategoryFriendlyUrlNotFoundException;

    ReadableEntityList<ReadableCategory> listByProduct(StoreMerchantId store, Long productId, LanguageCode language);

    boolean exists(StoreMerchantId store, String code);

    /**
     * Creates the category, or updates it when {@code id} is set. The body echoes back with its id.
     *
     * @throws CategoryReferenceUnresolvableException the parent reference names no category in this store
     */
    PersistableCategory save(StoreMerchantId store, PersistableCategory category)
            throws CategoryNotFoundException, CategoryReferenceUnresolvableException;

    void setVisible(StoreMerchantId store, Long id, boolean visible) throws CategoryNotFoundException;

    /**
     * Moves a category (and its subtree) under another; {@code parentId} of {@code -1} moves it to the root.
     */
    void move(StoreMerchantId store, Long id, Long parentId) throws CategoryNotFoundException;

    /**
     * Deletes the category and its subtree. Products left with no category are deleted with it.
     */
    void delete(StoreMerchantId store, Long id) throws CategoryNotFoundException;
}
