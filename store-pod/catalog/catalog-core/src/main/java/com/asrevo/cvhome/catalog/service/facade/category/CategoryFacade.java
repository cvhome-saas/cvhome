package com.asrevo.cvhome.catalog.service.facade.category;

import com.asrevo.cvhome.catalog.entity.category.Category;
import com.asrevo.cvhome.catalog.model.category.PersistableCategory;
import com.asrevo.cvhome.catalog.model.category.ReadableCategory;
import com.asrevo.cvhome.catalog.model.category.ReadableCategoryList;
import com.asrevo.cvhome.catalog.model.product.attribute.ReadableProductVariant;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.model.entity.ListCriteria;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import java.util.List;

public interface CategoryFacade {

    /**
     * Returns a list of ReadableCategory ordered and built according to a given depth
     *
     * @return ReadableCategoryList
     */
    ReadableCategoryList getReadableCategoryList(
            StoreMerchantId store,
            ListCriteria criteria,
            int depth,
            LanguageCode language,
            List<String> filter,
            int page,
            int count);

    /**
     * Returns a list of ReadableCategory ordered and built according to a given depth
     *
     * @return ReadableCategoryList
     */
    ReadableCategoryList getCategoryHierarchy(
            StoreMerchantId store,
            ListCriteria criteria,
            int depth,
            LanguageCode language,
            List<String> filter,
            int page,
            int count);

    /**
     * @return PersistableCategory
     */
    PersistableCategory saveCategory(StoreMerchantId store, PersistableCategory category);

    /**
     * @return ReadableCategory
     */
    ReadableCategory getById(StoreMerchantId store, Long id, LanguageCode language);

    /**
     * Get a Category by the Search Engine friendly URL slug
     */
    ReadableCategory getCategoryByFriendlyUrl(
            StoreMerchantId merchantStore, String friendlyUrl, LanguageCode language);

    Category getByCode(String code, StoreMerchantId store);

    void deleteCategory(Long categoryId, StoreMerchantId store);

    /**
     * List product options variations for a given category
     */
    List<ReadableProductVariant> categoryProductVariants(
            Long categoryId, StoreMerchantId store, LanguageCode language);

    /**
     * Check if category code already exists
     */
    boolean existByCode(StoreMerchantId store, String code);

    /**
     * Move a Category from a node to another node
     */
    void move(Long child, Long parent, StoreMerchantId store);

    /**
     * Set category visible or not
     */
    void setVisible(PersistableCategory category, StoreMerchantId store);

    /**
     * List category by product
     */
    ReadableCategoryList listByProduct(StoreMerchantId store, Long product, LanguageCode language);
}
