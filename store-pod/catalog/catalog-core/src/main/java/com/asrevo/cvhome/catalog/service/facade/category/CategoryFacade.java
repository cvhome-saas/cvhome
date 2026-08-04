package com.asrevo.cvhome.catalog.service.facade.category;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.asrevo.cvhome.catalog.entity.category.Category;
import com.asrevo.cvhome.catalog.errors.CategoryDescriptionLanguageMissingException;
import com.asrevo.cvhome.catalog.errors.CategoryFriendlyUrlNotFoundException;
import com.asrevo.cvhome.catalog.errors.CategoryIdentifiersInconsistentException;
import com.asrevo.cvhome.catalog.errors.CategoryNotConvertibleException;
import com.asrevo.cvhome.catalog.errors.CategoryNotFoundException;
import com.asrevo.cvhome.catalog.errors.CategoryReferenceUnresolvableException;
import com.asrevo.cvhome.catalog.errors.ForeignStoreProductAccessException;
import com.asrevo.cvhome.catalog.model.category.PersistableCategory;
import com.asrevo.cvhome.catalog.model.category.ReadableCategory;
import com.asrevo.cvhome.catalog.model.category.ReadableCategoryList;
import com.asrevo.cvhome.catalog.model.product.attribute.ReadableProductVariant;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.model.entity.ListCriteria;

public interface CategoryFacade {

    /**
     * Returns a list of ReadableCategory ordered and built according to a given depth
     *
     * @return ReadableCategoryList
     */
    ReadableCategoryList getReadableCategoryList(StoreMerchantId store, ListCriteria criteria, int depth,
                                                 LanguageCode language, List<String> filter, Pageable pageable);

    /**
     * Returns a list of ReadableCategory ordered and built according to a given depth
     *
     * @return ReadableCategoryList
     */
    ReadableCategoryList getCategoryHierarchy(StoreMerchantId store, ListCriteria criteria, int depth,
                                              LanguageCode language, List<String> filter, Pageable pageable);

    /**
     * @return PersistableCategory
     */
    PersistableCategory saveCategory(StoreMerchantId store, PersistableCategory category)
            throws CategoryNotConvertibleException,
            CategoryReferenceUnresolvableException, CategoryDescriptionLanguageMissingException;

    /**
     * @return ReadableCategory
     */
    ReadableCategory getById(StoreMerchantId store, Long id, LanguageCode language)
            throws CategoryNotFoundException, CategoryNotConvertibleException;

    /**
     * Get a Category by the Search Engine friendly URL slug
     */
    ReadableCategory getCategoryByFriendlyUrl(StoreMerchantId merchantStore, String friendlyUrl, LanguageCode language)
            throws CategoryFriendlyUrlNotFoundException;

    Category getByCode(String code, StoreMerchantId store);

    void deleteCategory(Long categoryId, StoreMerchantId store)
            throws CategoryNotFoundException;

    /**
     * List product options variations for a given category
     */
    List<ReadableProductVariant> categoryProductVariants(Long categoryId, StoreMerchantId store, LanguageCode language)
            throws CategoryNotFoundException;

    /**
     * Check if category code already exists
     */
    boolean existByCode(StoreMerchantId store, String code);

    /**
     * Move a Category from a node to another node
     */
    void move(Long child, Long parent, StoreMerchantId store)
            throws CategoryNotFoundException, CategoryIdentifiersInconsistentException,
            CategoryReferenceUnresolvableException;

    /**
     * Set category visible or not
     */
    void setVisible(PersistableCategory category, StoreMerchantId store)
            throws CategoryNotFoundException, ForeignStoreProductAccessException;

    /**
     * List category by product
     */
    ReadableCategoryList listByProduct(StoreMerchantId store, Long product, LanguageCode language)
            throws CategoryNotConvertibleException;

}
