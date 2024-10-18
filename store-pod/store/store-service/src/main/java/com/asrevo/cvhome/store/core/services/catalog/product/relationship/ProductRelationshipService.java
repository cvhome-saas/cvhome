package com.asrevo.cvhome.store.core.services.catalog.product.relationship;

import com.asrevo.cvhome.store.core.entity.catalog.product.Product;
import com.asrevo.cvhome.store.core.entity.catalog.product.relationship.ProductRelationship;
import com.asrevo.cvhome.store.core.entity.catalog.product.relationship.ProductRelationshipType;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityService;
import java.util.List;

public interface ProductRelationshipService
        extends SalesManagerEntityService<Long, ProductRelationship> {

    void saveOrUpdate(ProductRelationship relationship) throws ServiceException;

    /**
     * Get product relationship List for a given type (RELATED, FEATURED...) and language allows
     * to return the product description in the appropriate language
     *
     */
    List<ProductRelationship> getByType(
            MerchantStore store, Product product, ProductRelationshipType type, Language language)
            throws ServiceException;

    /**
     * Find by product and group name
     *
     */
    List<ProductRelationship> getByType(MerchantStore store, Product product, String name)
            throws ServiceException;

    /**
     * Get product relationship List for a given type (RELATED, FEATURED...) and a given base product
     *
     */
    List<ProductRelationship> getByType(
            MerchantStore store, Product product, ProductRelationshipType type)
            throws ServiceException;

    /**
     * Get product relationship List for a given type (RELATED, FEATURED...)
     *
     */
    List<ProductRelationship> getByType(MerchantStore store, ProductRelationshipType type)
            throws ServiceException;

    List<ProductRelationship> listByProduct(Product product) throws ServiceException;

    List<ProductRelationship> getByType(
            MerchantStore store, ProductRelationshipType type, Language language)
            throws ServiceException;

    /**
     * Get a list of relationship acting as groups of products
     *
     */
    List<ProductRelationship> getGroups(MerchantStore store);

    /**
     * Get a list of relationship acting as groups of products
     *
     */
    ProductRelationship getGroup(MerchantStore store, String code);

    /**
     * Get group by store and group name (code)
     *
     */
    List<ProductRelationship> getGroupDefinition(MerchantStore store, String name);

    /**
     * Creates a product group
     *
     */
    void addGroup(MerchantStore store, String groupName) throws ServiceException;

    List<ProductRelationship> getByGroup(MerchantStore store, String groupName)
            throws ServiceException;

    void deleteGroup(MerchantStore store, String groupName) throws ServiceException;

    void deactivateGroup(MerchantStore store, String groupName) throws ServiceException;

    void deleteRelationship(ProductRelationship relationship) throws ServiceException;

    void activateGroup(MerchantStore store, String groupName) throws ServiceException;

    List<ProductRelationship> getByGroup(MerchantStore store, String groupName, Language language)
            throws ServiceException;
}
