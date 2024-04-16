package com.asrevo.cvhome.store.service.facade.category;

import com.asrevo.cvhome.store.core.entity.catalog.category.Category;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.catalog.category.PersistableCategory;
import com.asrevo.cvhome.store.core.model.catalog.category.ReadableCategory;
import com.asrevo.cvhome.store.core.model.catalog.category.ReadableCategoryList;
import com.asrevo.cvhome.store.core.model.catalog.product.attribute.ReadableProductVariant;
import com.asrevo.cvhome.store.core.model.entity.ListCriteria;

import java.util.List;

public interface CategoryFacade {


    /**
     * Returns a list of ReadableCategory ordered and built according to a given depth
     *
     * @param store
     * @param depth
     * @param language
     * @param filter
     * @param page
     * @param count
     * @return ReadableCategoryList
     */
    ReadableCategoryList getReadableCategoryList(MerchantStore store, ListCriteria criteria, int depth, Language language, List<String> filter, int page, int count) ;

    /**
     * Returns a list of ReadableCategory ordered and built according to a given depth
     *
     * @param store
     * @param depth
     * @param language
     * @param filter
     * @param page
     * @param count
     * @return ReadableCategoryList
     */
    ReadableCategoryList getCategoryHierarchy(MerchantStore store, ListCriteria criteria, int depth, Language language, List<String> filter, int page, int count);

    /**
     * @param store
     * @param category
     * @return PersistableCategory
     */
    PersistableCategory saveCategory(MerchantStore store, PersistableCategory category);

    /**
     * @param store
     * @param id
     * @param language
     * @return ReadableCategory
     */
    ReadableCategory getById(MerchantStore store, Long id, Language language);

    /**
     * @param store
     * @param code
     * @param language
     * @return ReadableCategory
     * @throws Exception
     */
    ReadableCategory getByCode(MerchantStore store, String code, Language language) throws Exception;

    /**
     * Get a Category by the Search Engine friendly URL slug
     *
     * @param merchantStore
     * @param friendlyUrl
     * @param language
     * @return
     */
    ReadableCategory getCategoryByFriendlyUrl(MerchantStore merchantStore, String friendlyUrl, Language language) throws Exception;

    Category getByCode(String code, MerchantStore store);

    void deleteCategory(Long categoryId, MerchantStore store);

    void deleteCategory(Category category);


    /**
     * List product options variations for a given category
     *
     * @param categoryId
     * @param store
     * @param language
     * @return
     */
    List<ReadableProductVariant> categoryProductVariants(Long categoryId, MerchantStore store, Language language);

    /**
     * Check if category code already exist
     *
     * @param store
     * @param code
     * @return
     * @throws Exception
     */
    boolean existByCode(MerchantStore store, String code);

    /**
     * Move a Category from a node to another node
     *
     * @param child
     * @param parent
     * @param store
     */
    void move(Long child, Long parent, MerchantStore store);

    /**
     * Set category visible or not
     *
     * @param category
     * @param store
     */
    void setVisible(PersistableCategory category, MerchantStore store);


    /**
     * List category by product
     *
     * @param store
     * @param product
     * @return
     */
    ReadableCategoryList listByProduct(MerchantStore store, Long product, Language language);
}
